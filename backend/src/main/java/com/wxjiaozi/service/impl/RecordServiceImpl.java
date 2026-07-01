package com.wxjiaozi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxjiaozi.common.BusinessException;
import com.wxjiaozi.dto.mini.QuestionSessionDTO;
import com.wxjiaozi.dto.mini.SubjectDTO;
import com.wxjiaozi.dto.mini.SubmitAnswerDTO;
import com.wxjiaozi.dto.mini.SubmitResultDTO;
import com.wxjiaozi.entity.ExamQuestion;
import com.wxjiaozi.entity.ExamRecord;
import com.wxjiaozi.entity.ExamSubject;
import com.wxjiaozi.mapper.ExamQuestionMapper;
import com.wxjiaozi.mapper.ExamRecordMapper;
import com.wxjiaozi.mapper.ExamSubjectMapper;
import com.wxjiaozi.service.RecordService;
import com.wxjiaozi.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户做题记录服务实现
 */
@Slf4j
@Service
public class RecordServiceImpl implements RecordService {

    private final ExamQuestionMapper examQuestionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ExamSubjectMapper examSubjectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public RecordServiceImpl(ExamQuestionMapper examQuestionMapper,
                             ExamRecordMapper examRecordMapper,
                             ExamSubjectMapper examSubjectMapper,
                             StringRedisTemplate stringRedisTemplate,
                             ObjectMapper objectMapper) {
        this.examQuestionMapper = examQuestionMapper;
        this.examRecordMapper = examRecordMapper;
        this.examSubjectMapper = examSubjectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    // ════════════════════════════════════════════════════════════
    // 1. 提交答题
    // ════════════════════════════════════════════════════════════

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmitResultDTO submitAnswer(Long userId, SubmitAnswerDTO params) {
        // 1. 获取题目
        ExamQuestion question = examQuestionMapper.selectById(params.getQuestionId());
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        // 2. 判断答案是否正确
        List<String> correctAnswerList = parseAnswerToList(question.getAnswer());
        List<String> selectedOptions = params.getSelectedOptions();
        boolean isCorrect = isAnswerCorrect(correctAnswerList, selectedOptions);

        // 3. 写入exam_record（利用唯一索引去重）
        boolean isFirstTime = false;
        try {
            ExamRecord record = ExamRecord.builder()
                    .userId(userId)
                    .subjectId(question.getSubjectId())
                    .questionId(params.getQuestionId())
                    .isCorrect(isCorrect ? 1 : 0)
                    .build();
            // insertOrUpdate: ON DUPLICATE KEY UPDATE
            // 返回1=新插入, 2=更新了已有行
            int affected = examRecordMapper.insertOrUpdate(record);
            isFirstTime = (affected == 1);
        } catch (DuplicateKeyException e) {
            // 并发安全：唯一索引冲突时说明已有记录，不计入首次
            log.info("并发重复答题: userId={}, questionId={}", userId, params.getQuestionId());
            isFirstTime = false;
        }

        // 4. 首次做题才同步Redis INCR
        if (isFirstTime) {
            String doneKey = RedisKeyUtil.userSubjectDoneKey(userId, question.getSubjectId());
            try {
                Long newCount = stringRedisTemplate.opsForValue().increment(doneKey);
                // 首次INCR后设置过期时间（仅首次设置，避免每次覆盖TTL）
                if (newCount != null && newCount == 1) {
                    stringRedisTemplate.expire(doneKey, RedisKeyUtil.USER_SUBJECT_DONE_TTL, TimeUnit.SECONDS);
                }
                log.info("用户首次做题 Redis INCR: userId={}, subjectId={}, doneCount={}",
                        userId, question.getSubjectId(), newCount);
            } catch (Exception e) {
                // Redis异常不影响主流程，下次查库回填
                log.error("Redis INCR失败: userId={}, subjectId={}", userId, question.getSubjectId(), e);
            }
        }

        // 5. 构造返回结果
        SubmitResultDTO result = new SubmitResultDTO();
        result.setCorrect(isCorrect);
        result.setCorrectAnswer(correctAnswerList);
        result.setExplanation(question.getExplanation());
        result.setAnalysis(question.getExplanation());
        return result;
    }

    // ════════════════════════════════════════════════════════════
    // 2. 首页科目列表（含用户已做题数）
    // ════════════════════════════════════════════════════════════

    @Override
    public List<SubjectDTO> getSubjectList(Long userId, Long categoryId) {
        // 1. 查询所有启用科目
        List<ExamSubject> subjects = examSubjectMapper.selectByCategoryId(categoryId);
        if (subjects.isEmpty()) {
            return Collections.emptyList();
        }

        List<SubjectDTO> result = new ArrayList<>(subjects.size());
        for (ExamSubject subject : subjects) {
            Long doneCount = 0L;

            // 2. 如果用户已登录，获取其已做题数
            if (userId != null) {
                doneCount = getDoneCount(userId, subject.getId());
            }

            // 3. 构造DTO
            SubjectDTO dto = SubjectDTO.builder()
                    .id(subject.getId())
                    .categoryId(subject.getCategoryId())
                    .name(subject.getName())
                    .icon(subject.getIcon())
                    .sortOrder(subject.getSortOrder())
                    .status(subject.getStatus())
                    .totalQuestions(subject.getTotalQuestions())
                    .doneCount(doneCount)
                    .build();
            result.add(dto);
        }
        return result;
    }

    /**
     * 获取用户某科目已做题数
     * <p>
     * 优先读Redis缓存，缓存不存在则查MySQL统计并回填。
     * </p>
     */
    private Long getDoneCount(Long userId, Long subjectId) {
        String doneKey = RedisKeyUtil.userSubjectDoneKey(userId, subjectId);

        // 1. 优先读Redis
        try {
            String cached = stringRedisTemplate.opsForValue().get(doneKey);
            if (StrUtil.isNotBlank(cached)) {
                return Long.parseLong(cached);
            }
        } catch (Exception e) {
            log.warn("Redis读取失败，降级查MySQL: key={}", doneKey, e);
        }

        // 2. 缓存未命中，查MySQL统计
        Long doneCount;
        try {
            doneCount = examRecordMapper.countByUserIdAndSubjectId(userId, subjectId);
        } catch (Exception e) {
            log.error("MySQL统计失败: userId={}, subjectId={}", userId, subjectId, e);
            doneCount = 0L;
        }

        // 3. 回填Redis缓存
        try {
            stringRedisTemplate.opsForValue().set(
                    doneKey,
                    String.valueOf(doneCount),
                    RedisKeyUtil.USER_SUBJECT_DONE_TTL,
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.warn("Redis回填失败: key={}", doneKey, e);
        }

        return doneCount;
    }

    // ════════════════════════════════════════════════════════════
    // 3. 删除做题记录
    // ════════════════════════════════════════════════════════════

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecord(Long userId, Long questionId) {
        // 1. 先查出记录获取subjectId
        ExamRecord record = examRecordMapper.selectByUserIdAndQuestionId(userId, questionId);
        if (record == null) {
            throw new BusinessException("做题记录不存在");
        }

        // 2. 删除MySQL记录
        int deleted = examRecordMapper.deleteByUserIdAndQuestionId(userId, questionId);
        if (deleted == 0) {
            throw new BusinessException("删除失败，记录不存在");
        }

        // 3. 同步Redis DECR
        String doneKey = RedisKeyUtil.userSubjectDoneKey(userId, record.getSubjectId());
        try {
            Long newCount = stringRedisTemplate.opsForValue().decrement(doneKey);
            // 防止计数为负
            if (newCount != null && newCount < 0) {
                stringRedisTemplate.opsForValue().set(doneKey, "0",
                        RedisKeyUtil.USER_SUBJECT_DONE_TTL, TimeUnit.SECONDS);
            }
            log.info("删除做题记录 Redis DECR: userId={}, subjectId={}, newCount={}",
                    userId, record.getSubjectId(), newCount);
        } catch (Exception e) {
            // Redis异常：删除缓存让下次查库回填
            stringRedisTemplate.delete(doneKey);
            log.warn("Redis DECR失败，已删除缓存: key={}", doneKey, e);
        }
    }

    // ════════════════════════════════════════════════════════════
    // 答案比对工具方法
    // ════════════════════════════════════════════════════════════

    /**
     * 解析答案字段为选项列表
     */
    private List<String> parseAnswerToList(String answer) {
        if (StrUtil.isBlank(answer)) {
            return Collections.emptyList();
        }
        String trimmed = answer.trim();
        // 判断题
        if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
            return Collections.singletonList(trimmed.toLowerCase());
        }
        // 多选
        if (trimmed.contains(",")) {
            return StrUtil.split(trimmed, ',', true, true);
        }
        // 单选
        return Collections.singletonList(trimmed.toUpperCase());
    }

    /**
     * 判断答案是否正确（排序后比对，顺序无关）
     */
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
