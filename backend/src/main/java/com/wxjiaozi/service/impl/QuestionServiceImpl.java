package com.wxjiaozi.service.impl;

import com.wxjiaozi.common.BusinessException;
import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.dto.mini.DailyQuestionDTO;
import com.wxjiaozi.dto.mini.QuestionDTO;
import com.wxjiaozi.dto.mini.SubjectStatsDTO;
import com.wxjiaozi.util.OptionListConverter;
import com.wxjiaozi.entity.ExamQuestion;
import com.wxjiaozi.entity.UserCollection;
import com.wxjiaozi.mapper.ExamQuestionMapper;
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

/**
 * 题目服务（MyBatis 原生版）
 */
@Slf4j
@Service
public class QuestionServiceImpl implements QuestionService {

    private final ExamQuestionMapper examQuestionMapper;
    private final UserCollectionMapper userCollectionMapper;

    public QuestionServiceImpl(ExamQuestionMapper examQuestionMapper,
                               UserCollectionMapper userCollectionMapper) {
        this.examQuestionMapper = examQuestionMapper;
        this.userCollectionMapper = userCollectionMapper;
    }

    @Override
    public PageResult<QuestionDTO> getQuestionList(Long subjectId, Integer type,
                                                   Integer status, String keyword, int page, int pageSize) {
        // 原生分页计算：offset = (page - 1) * pageSize
        int offset = (page - 1) * pageSize;
        List<ExamQuestion> questions = examQuestionMapper.selectPageWithFilters(
                offset, pageSize, subjectId, type, status, keyword);
        Long total = examQuestionMapper.countWithFilters(subjectId, type, status, keyword);

        List<QuestionDTO> dtoList = new ArrayList<>();
        for (ExamQuestion q : questions) {
            dtoList.add(convertToDTO(q));
        }

        return new PageResult<>(dtoList, total, page, pageSize);
    }

    @Override
    public PageResult<QuestionDTO> getCollectedQuestions(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<UserCollection> collections = userCollectionMapper.selectByUserIdWithPage(
                userId, offset, pageSize);
        Long total = userCollectionMapper.countByUserId(userId);

        List<QuestionDTO> dtoList = new ArrayList<>();
        for (UserCollection uc : collections) {
            ExamQuestion question = examQuestionMapper.selectById(uc.getQuestionId());
            if (question != null) {
                dtoList.add(convertToDTO(question));
            }
        }

        return new PageResult<>(dtoList, total, page, pageSize);
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

        // 原生 MyBatis 查询总记录数（categoryId 暂时不使用，因为没有分类关联）
        Long total = examQuestionMapper.countWithFilters(null, null, 1, null);
        if (total == 0) {
            throw new BusinessException("没有可用题目");
        }

        Random random = new Random(seed);
        int skip = random.nextInt(total.intValue());

        // 使用 LIMIT 1 OFFSET skip 获取每日一题
        ExamQuestion question = examQuestionMapper.selectByOffset(skip);
        if (question == null) {
            question = examQuestionMapper.selectByOffset(0);
        }

        DailyQuestionDTO dto = new DailyQuestionDTO();
        dto.setDate(today.toString());
        dto.setQuestion(convertToDTO(question));
        dto.setAnswered(false);
        return dto;
    }

    @Override
    public PageResult<QuestionDTO> searchQuestions(String keyword, Long categoryId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<ExamQuestion> questions = examQuestionMapper.selectPageWithFilters(
                offset, pageSize, null, null, 1, keyword);
        Long total = examQuestionMapper.countWithFilters(null, null, 1, keyword);

        List<QuestionDTO> dtoList = new ArrayList<>();
        for (ExamQuestion q : questions) {
            dtoList.add(convertToDTO(q));
        }

        return new PageResult<>(dtoList, total, page, pageSize);
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
        dto.setOptionList(OptionListConverter.toDTOList(q.getOptionList()));
        dto.setAnswer(q.getAnswer());
        dto.setExplanation(q.getExplanation());
        dto.setStatus(q.getStatus());
        return dto;
    }
}
