package com.wxjiaozi.service;

import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.dto.mini.SubjectStatsDTO;
import com.wxjiaozi.dto.mini.DailyQuestionDTO;
import com.wxjiaozi.dto.mini.QuestionDTO;

public interface QuestionService {

    PageResult<QuestionDTO> getQuestionList(Long categoryId, Long subjectId, Long chapterId, Long tagId,
                                            Integer type, Integer difficulty, String keyword, int page, int pageSize);

    PageResult<QuestionDTO> getCollectedQuestions(Long userId, int page, int pageSize);

    void collectQuestion(Long userId, Long questionId);

    void uncollectQuestion(Long userId, Long questionId);

    DailyQuestionDTO getDailyQuestion(Long categoryId);

    PageResult<QuestionDTO> searchQuestions(String keyword, Long categoryId, int page, int pageSize);

    /**
     * 获取科目题目统计（按题型）
     */
    SubjectStatsDTO getSubjectStats(Long subjectId);
}
