package com.wxjiaozi.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxjiaozi.common.BusinessException;
import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.dto.mini.DailyQuestionDTO;
import com.wxjiaozi.dto.mini.QuestionDTO;
import com.wxjiaozi.dto.mini.SubjectStatsDTO;
import com.wxjiaozi.entity.ExamQuestion;
import com.wxjiaozi.entity.UserAnswerRecord;
import com.wxjiaozi.entity.UserCollection;
import com.wxjiaozi.mapper.ExamQuestionMapper;
import com.wxjiaozi.mapper.UserAnswerRecordMapper;
import com.wxjiaozi.mapper.UserCollectionMapper;
import com.wxjiaozi.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
public class QuestionServiceImpl implements QuestionService {

    private final ExamQuestionMapper examQuestionMapper;
    private final UserCollectionMapper userCollectionMapper;
    private final UserAnswerRecordMapper userAnswerRecordMapper;

    public QuestionServiceImpl(ExamQuestionMapper examQuestionMapper,
                               UserCollectionMapper userCollectionMapper,
                               UserAnswerRecordMapper userAnswerRecordMapper) {
        this.examQuestionMapper = examQuestionMapper;
        this.userCollectionMapper = userCollectionMapper;
        this.userAnswerRecordMapper = userAnswerRecordMapper;
    }

    @Override
    public PageResult<QuestionDTO> getQuestionList(Long subjectId, Integer type,
                                                   Integer status, String keyword, int page, int pageSize) {
        Page<ExamQuestion> mpPage = new Page<>(page, pageSize);
        IPage<ExamQuestion> result = examQuestionMapper.selectPageWithFilters(
                mpPage, subjectId, type, status, keyword);

        List<QuestionDTO> dtoList = new ArrayList<>();
        for (ExamQuestion q : result.getRecords()) {
            dtoList.add(convertToDTO(q));
        }

        Page<QuestionDTO> dtoPage = new Page<>(page, pageSize, result.getTotal());
        dtoPage.setRecords(dtoList);
        return PageResult.of(dtoPage);
    }

    @Override
    public PageResult<QuestionDTO> getCollectedQuestions(Long userId, int page, int pageSize) {
        Page<UserCollection> collectionPage = new Page<>(page, pageSize);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserCollection> wrapper
                = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(UserCollection::getUserId, userId);
        wrapper.orderByDesc(UserCollection::getCreateTime);
        IPage<UserCollection> collectionResult = userCollectionMapper.selectPage(collectionPage, wrapper);

        List<QuestionDTO> dtoList = new ArrayList<>();
        for (UserCollection uc : collectionResult.getRecords()) {
            ExamQuestion question = examQuestionMapper.selectById(uc.getQuestionId());
            if (question != null) {
                dtoList.add(convertToDTO(question));
            }
        }

        Page<QuestionDTO> dtoPage = new Page<>(page, pageSize, collectionResult.getTotal());
        dtoPage.setRecords(dtoList);
        return PageResult.of(dtoPage);
    }

    @Override
    @Transactional
    public void collectQuestion(Long userId, Long questionId) {
        ExamQuestion question = examQuestionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        UserCollection existing = userCollectionMapper.selectByUserIdAndQuestionId(userId, questionId);
        if (existing != null) {
            return;
        }

        UserCollection collection = new UserCollection();
        collection.setUserId(userId);
        collection.setQuestionId(questionId);
        userCollectionMapper.insert(collection);
    }

    @Override
    @Transactional
    public void uncollectQuestion(Long userId, Long questionId) {
        UserCollection existing = userCollectionMapper.selectByUserIdAndQuestionId(userId, questionId);
        if (existing != null) {
            userCollectionMapper.deleteById(existing.getId());
        }
    }

    @Override
    public DailyQuestionDTO getDailyQuestion(Long categoryId) {
        LocalDate today = LocalDate.now();
        long seed = today.toEpochDay();

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ExamQuestion> wrapper
                = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(ExamQuestion::getStatus, 1);

        long total = examQuestionMapper.selectCount(wrapper);
        if (total == 0) {
            throw new BusinessException("没有可用题目");
        }

        Random random = new Random(seed);
        int skip = random.nextInt((int) total);

        Page<ExamQuestion> page = new Page<>(skip / 10 + 1, 10);
        IPage<ExamQuestion> result = examQuestionMapper.selectPage(page, wrapper);

        ExamQuestion question;
        if (result.getRecords().isEmpty()) {
            question = examQuestionMapper.selectList(wrapper).get(0);
        } else {
            int index = skip % 10;
            if (index >= result.getRecords().size()) {
                index = 0;
            }
            question = result.getRecords().get(index);
        }

        DailyQuestionDTO dto = new DailyQuestionDTO();
        dto.setDate(today.toString());
        dto.setQuestion(convertToDTO(question));
        dto.setAnswered(false);
        return dto;
    }

    @Override
    public PageResult<QuestionDTO> searchQuestions(String keyword, Long categoryId, int page, int pageSize) {
        Page<ExamQuestion> mpPage = new Page<>(page, pageSize);
        IPage<ExamQuestion> result = examQuestionMapper.selectPageWithFilters(
                mpPage, null, null, 1, keyword);

        List<QuestionDTO> dtoList = new ArrayList<>();
        for (ExamQuestion q : result.getRecords()) {
            dtoList.add(convertToDTO(q));
        }

        Page<QuestionDTO> dtoPage = new Page<>(page, pageSize, result.getTotal());
        dtoPage.setRecords(dtoList);
        return PageResult.of(dtoPage);
    }

    @Override
    public SubjectStatsDTO getSubjectStats(Long subjectId) {
        List<Map<String, Object>> rows = examQuestionMapper.countBySubjectAndType(subjectId);

        SubjectStatsDTO stats = new SubjectStatsDTO();
        int total = 0;
        for (Map<String, Object> row : rows) {
            Integer type = (Integer) row.get("type");
            Object countObj = row.get("count");
            int count = countObj instanceof Long ? ((Long) countObj).intValue() : (Integer) countObj;

            total += count;
            if (type == 1) {
                stats.setSingleCount(count);
            } else if (type == 2) {
                stats.setMultiCount(count);
            } else if (type == 3) {
                stats.setTrueFalseCount(count);
            }
        }
        stats.setTotalCount(total);
        return stats;
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
}
