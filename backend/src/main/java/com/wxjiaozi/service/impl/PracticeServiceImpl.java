package com.wxjiaozi.service.impl;

import cn.hutool.core.util.StrUtil;
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
import com.wxjiaozi.enums.SubjectEnum;
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
        // 校验科目编码合法性
        if (!SubjectEnum.isValidCode(params.getSubjectId().intValue())) {
            throw new BusinessException("非法的科目编码");
        }

        List<ExamQuestion> selectedQuestions;

        // 随机试卷模式：按题型分别抽取指定数量
        if ("random".equals(params.getMode()) && params.getSingleCount() != null
                && params.getMultiCount() != null && params.getTrueFalseCount() != null) {
            selectedQuestions = selectRandomExamQuestions(params);
        } else {
            // 普通练习模式：按科目随机抽取
            List<ExamQuestion> allQuestions = examQuestionMapper.selectRandomBySubjectAndType(
                    params.getSubjectId(),
                    null,
                    params.getQuestionCount() != null && params.getQuestionCount() > 0
                            ? params.getQuestionCount()
                            : 50  // 默认50题
            );
            if (allQuestions.isEmpty()) {
                throw new BusinessException("该科目下没有可用题目");
            }
            selectedQuestions = allQuestions;
        }

        String sessionId = UUID.randomUUID().toString().replace("-", "");

        List<QuestionDTO> questionDTOs = new ArrayList<>();
        for (ExamQuestion q : selectedQuestions) {
            QuestionDTO dto = convertToDTO(q);
            dto.setAnswer(null);
            questionDTOs.add(dto);
        }

        QuestionSessionDTO session = new QuestionSessionDTO();
        session.setSessionId(sessionId);
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

    /**
     * 随机试卷模式：按题型分别抽取指定数量
     * 单选 → 多选 → 判断 顺序排列
     */
    private List<ExamQuestion> selectRandomExamQuestions(StartSessionDTO params) {
        List<ExamQuestion> result = new ArrayList<>();

        // 单选题
        if (params.getSingleCount() != null && params.getSingleCount() > 0) {
            List<ExamQuestion> singles = examQuestionMapper.selectRandomBySubjectAndType(
                    params.getSubjectId(), 1, params.getSingleCount());
            if (singles.isEmpty()) {
                throw new BusinessException("该科目下单选题数量不足");
            }
            if (singles.size() < params.getSingleCount()) {
                throw new BusinessException("该科目下单选题数量不足，需要 "
                        + params.getSingleCount() + " 题，实际只有 " + singles.size() + " 题");
            }
            result.addAll(singles);
        }

        // 多选题
        if (params.getMultiCount() != null && params.getMultiCount() > 0) {
            List<ExamQuestion> multis = examQuestionMapper.selectRandomBySubjectAndType(
                    params.getSubjectId(), 2, params.getMultiCount());
            if (multis.isEmpty()) {
                throw new BusinessException("该科目下多选题数量不足");
            }
            if (multis.size() < params.getMultiCount()) {
                throw new BusinessException("该科目下多选题数量不足，需要 "
                        + params.getMultiCount() + " 题，实际只有 " + multis.size() + " 题");
            }
            result.addAll(multis);
        }

        // 判断题
        if (params.getTrueFalseCount() != null && params.getTrueFalseCount() > 0) {
            List<ExamQuestion> tfs = examQuestionMapper.selectRandomBySubjectAndType(
                    params.getSubjectId(), 3, params.getTrueFalseCount());
            if (tfs.isEmpty()) {
                throw new BusinessException("该科目下判断题数量不足");
            }
            if (tfs.size() < params.getTrueFalseCount()) {
                throw new BusinessException("该科目下判断题数量不足，需要 "
                        + params.getTrueFalseCount() + " 题，实际只有 " + tfs.size() + " 题");
            }
            result.addAll(tfs);
        }

        if (result.isEmpty()) {
            throw new BusinessException("该科目下没有可用题目");
        }

        return result;
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

        List<UserAnswerRecord> records = userAnswerRecordMapper.selectBySessionIdAndUserId(params.getSessionId(), userId);

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
        dto.setSubjectId(q.getSubjectId());
        dto.setType(q.getType());
        dto.setStem(q.getStem());
        dto.setOptionList(q.getOptionList());
        dto.setAnswer(q.getAnswer());
        dto.setExplanation(q.getExplanation());
        dto.setStatus(q.getStatus());
        return dto;
    }

    private List<String> parseAnswerToList(String answer) {
        if (StrUtil.isBlank(answer)) {
            return Collections.emptyList();
        }
        String trimmed = answer.trim();
        // 判断题特殊处理：true/false
        if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
            return Collections.singletonList(trimmed.toLowerCase());
        }
        // 多选：多个字母用逗号分隔
        if (trimmed.contains(",")) {
            return cn.hutool.core.util.StrUtil.split(trimmed, ',', true, true);
        }
        // 单选：单个字母
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
