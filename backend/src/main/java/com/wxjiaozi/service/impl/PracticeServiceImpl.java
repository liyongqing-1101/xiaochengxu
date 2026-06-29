package com.wxjiaozi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxjiaozi.common.BusinessException;
import com.wxjiaozi.dto.mini.EndSessionDTO;
import com.wxjiaozi.dto.mini.QuestionDTO;
import com.wxjiaozi.dto.mini.QuestionSessionDTO;
import com.wxjiaozi.dto.mini.SessionSummaryDTO;
import com.wxjiaozi.dto.mini.StartSessionDTO;
import com.wxjiaozi.dto.mini.SubmitAnswerDTO;
import com.wxjiaozi.dto.mini.SubmitResultDTO;
import com.wxjiaozi.entity.ExamQuestion;
import com.wxjiaozi.entity.ExamSubject;
import com.wxjiaozi.entity.UserAnswerRecord;
import com.wxjiaozi.entity.UserWrongQuestion;
import com.wxjiaozi.mapper.ExamQuestionMapper;
import com.wxjiaozi.mapper.ExamSubjectMapper;
import com.wxjiaozi.mapper.UserAnswerRecordMapper;
import com.wxjiaozi.mapper.UserWrongQuestionMapper;
import com.wxjiaozi.service.PracticeService;
import com.wxjiaozi.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PracticeServiceImpl implements PracticeService {

    private final ExamQuestionMapper examQuestionMapper;
    private final ExamSubjectMapper examSubjectMapper;
    private final UserAnswerRecordMapper userAnswerRecordMapper;
    private final UserWrongQuestionMapper userWrongQuestionMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public PracticeServiceImpl(ExamQuestionMapper examQuestionMapper,
                               ExamSubjectMapper examSubjectMapper,
                               UserAnswerRecordMapper userAnswerRecordMapper,
                               UserWrongQuestionMapper userWrongQuestionMapper,
                               StringRedisTemplate stringRedisTemplate,
                               ObjectMapper objectMapper) {
        this.examQuestionMapper = examQuestionMapper;
        this.examSubjectMapper = examSubjectMapper;
        this.userAnswerRecordMapper = userAnswerRecordMapper;
        this.userWrongQuestionMapper = userWrongQuestionMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public QuestionSessionDTO startSession(Long userId, StartSessionDTO params) {
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestion::getCategoryId, params.getCategoryId());
        wrapper.eq(ExamQuestion::getSubjectId, params.getSubjectId());
        wrapper.eq(ExamQuestion::getStatus, 1);

        if (params.getChapterId() != null) {
            wrapper.eq(ExamQuestion::getChapterId, params.getChapterId());
        }
        if (params.getKnowledgePointId() != null) {
            wrapper.eq(ExamQuestion::getTagId, params.getKnowledgePointId());
        }

        List<ExamQuestion> allQuestions = examQuestionMapper.selectList(wrapper);
        if (allQuestions.isEmpty()) {
            throw new BusinessException("该条件下没有可用题目");
        }

        Collections.shuffle(allQuestions);

        int questionCount = params.getQuestionCount() != null && params.getQuestionCount() > 0
                ? Math.min(params.getQuestionCount(), allQuestions.size())
                : allQuestions.size();

        List<ExamQuestion> selectedQuestions = allQuestions.subList(0, questionCount);

        String sessionId = UUID.randomUUID().toString().replace("-", "");

        List<QuestionDTO> questionDTOs = new ArrayList<>();
        for (ExamQuestion q : selectedQuestions) {
            QuestionDTO dto = convertToDTO(q);
            dto.setAnswer(null);
            questionDTOs.add(dto);
        }

        QuestionSessionDTO session = new QuestionSessionDTO();
        session.setSessionId(sessionId);
        session.setCategoryId(params.getCategoryId());
        session.setSubjectId(params.getSubjectId());
        session.setCurrentIndex(0);
        session.setQuestions(questionDTOs);

        try {
            String sessionJson = objectMapper.writeValueAsString(session);
            stringRedisTemplate.opsForValue().set(
                    RedisKeyUtil.sessionKey(sessionId),
                    sessionJson,
                    RedisKeyUtil.SESSION_TTL,
                    TimeUnit.SECONDS
            );
        } catch (JsonProcessingException e) {
            log.error("序列化会话失败", e);
            throw new BusinessException("创建答题会话失败");
        }

        return session;
    }

    @Override
    @Transactional
    public SubmitResultDTO submitAnswer(Long userId, SubmitAnswerDTO params) {
        String sessionJson = stringRedisTemplate.opsForValue().get(RedisKeyUtil.sessionKey(params.getSessionId()));
        if (StrUtil.isBlank(sessionJson)) {
            throw new BusinessException("会话已过期或不存在");
        }

        QuestionSessionDTO session;
        try {
            session = objectMapper.readValue(sessionJson, QuestionSessionDTO.class);
        } catch (JsonProcessingException e) {
            log.error("反序列化会话失败", e);
            throw new BusinessException("会话数据异常");
        }

        ExamQuestion question = examQuestionMapper.selectById(params.getQuestionId());
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        List<String> correctAnswerList = parseAnswerToList(question.getAnswer());
        List<String> selectedOptions = params.getSelectedOptions();

        boolean isCorrect = isAnswerCorrect(correctAnswerList, selectedOptions);

        UserAnswerRecord record = new UserAnswerRecord();
        record.setUserId(userId);
        record.setQuestionId(params.getQuestionId());
        record.setCategoryId(question.getCategoryId());
        record.setSubjectId(question.getSubjectId());
        record.setSessionId(params.getSessionId());
        record.setSelectedOptions(String.join(",", selectedOptions));
        record.setIsCorrect(isCorrect ? 1 : 0);
        record.setDuration(params.getDuration());
        userAnswerRecordMapper.insert(record);

        if (!isCorrect) {
            UserWrongQuestion wrongQuestion = userWrongQuestionMapper.selectByUserIdAndQuestionId(userId, params.getQuestionId());
            if (wrongQuestion == null) {
                wrongQuestion = new UserWrongQuestion();
                wrongQuestion.setUserId(userId);
                wrongQuestion.setQuestionId(params.getQuestionId());
                wrongQuestion.setErrorCount(1);
                wrongQuestion.setLastWrongAnswer(String.join(",", selectedOptions));
                wrongQuestion.setSource(0);
                userWrongQuestionMapper.insert(wrongQuestion);
            } else {
                wrongQuestion.setErrorCount(wrongQuestion.getErrorCount() + 1);
                wrongQuestion.setLastWrongAnswer(String.join(",", selectedOptions));
                userWrongQuestionMapper.updateById(wrongQuestion);
            }
        }

        session.setCurrentIndex(session.getCurrentIndex() + 1);
        try {
            stringRedisTemplate.opsForValue().set(
                    RedisKeyUtil.sessionKey(params.getSessionId()),
                    objectMapper.writeValueAsString(session),
                    RedisKeyUtil.SESSION_TTL,
                    TimeUnit.SECONDS
            );
        } catch (JsonProcessingException e) {
            log.error("更新会话Redis失败", e);
        }

        SubmitResultDTO result = new SubmitResultDTO();
        result.setCorrect(isCorrect);
        result.setCorrectAnswer(correctAnswerList);
        result.setExplanation(question.getExplanation());
        result.setAnalysis(question.getExplanation());
        return result;
    }

    @Override
    public SessionSummaryDTO endSession(Long userId, EndSessionDTO params) {
        String sessionJson = stringRedisTemplate.opsForValue().get(RedisKeyUtil.sessionKey(params.getSessionId()));
        if (StrUtil.isBlank(sessionJson)) {
            throw new BusinessException("会话已过期或不存在");
        }

        QuestionSessionDTO session;
        try {
            session = objectMapper.readValue(sessionJson, QuestionSessionDTO.class);
        } catch (JsonProcessingException e) {
            log.error("反序列化会话失败", e);
            throw new BusinessException("会话数据异常");
        }

        LambdaQueryWrapper<UserAnswerRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAnswerRecord::getSessionId, params.getSessionId());
        wrapper.eq(UserAnswerRecord::getUserId, userId);
        List<UserAnswerRecord> records = userAnswerRecordMapper.selectList(wrapper);

        int totalQuestions = session.getQuestions().size();
        int correctCount = (int) records.stream()
                .filter(r -> r.getIsCorrect() != null && r.getIsCorrect() == 1)
                .count();
        double accuracy = totalQuestions > 0 ? (double) correctCount / totalQuestions : 0.0;
        long totalDuration = records.stream()
                .filter(r -> r.getDuration() != null)
                .mapToLong(UserAnswerRecord::getDuration)
                .sum();

        String subjectName = "";
        if (session.getSubjectId() != null) {
            ExamSubject subject = examSubjectMapper.selectById(session.getSubjectId());
            if (subject != null) {
                subjectName = subject.getName();
            }
        }

        stringRedisTemplate.delete(RedisKeyUtil.sessionKey(params.getSessionId()));

        SessionSummaryDTO summary = new SessionSummaryDTO();
        summary.setSessionId(params.getSessionId());
        summary.setTotalQuestions(totalQuestions);
        summary.setCorrectCount(correctCount);
        summary.setAccuracy(Math.round(accuracy * 10000.0) / 10000.0);
        summary.setDuration(totalDuration);
        summary.setSubjectId(session.getSubjectId());
        summary.setSubjectName(subjectName);
        summary.setCreatedAt(LocalDateTime.now());
        return summary;
    }

    private QuestionDTO convertToDTO(ExamQuestion q) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(q.getId());
        dto.setCategoryId(q.getCategoryId());
        dto.setSubjectId(q.getSubjectId());
        dto.setChapterId(q.getChapterId());
        dto.setTagId(q.getTagId());
        dto.setType(q.getType());
        dto.setStem(q.getStem());
        dto.setOptionA(q.getOptionA());
        dto.setOptionB(q.getOptionB());
        dto.setOptionC(q.getOptionC());
        dto.setOptionD(q.getOptionD());
        dto.setAnswer(q.getAnswer());
        dto.setExplanation(q.getExplanation());
        dto.setDifficulty(q.getDifficulty());
        dto.setStatus(q.getStatus());
        return dto;
    }

    private List<String> parseAnswerToList(String answer) {
        if (StrUtil.isBlank(answer)) {
            return Collections.emptyList();
        }
        String trimmed = answer.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                return objectMapper.readValue(trimmed, new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                log.warn("解析JSON答案失败: {}", answer);
            }
        }
        return Collections.singletonList(trimmed.toUpperCase());
    }

    private boolean isAnswerCorrect(List<String> correctAnswer, List<String> selectedOptions) {
        if (correctAnswer == null || selectedOptions == null) {
            return false;
        }
        if (correctAnswer.size() != selectedOptions.size()) {
            return false;
        }
        List<String> normalizedCorrect = correctAnswer.stream()
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.toList());
        List<String> normalizedSelected = selectedOptions.stream()
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.toList());
        return normalizedCorrect.equals(normalizedSelected);
    }
}
