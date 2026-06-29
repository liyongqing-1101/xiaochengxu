package com.wxjiaozi.service;

import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.dto.admin.AdminLoginResultDTO;
import com.wxjiaozi.dto.admin.QuestionSaveDTO;
import com.wxjiaozi.entity.ExamCategory;
import com.wxjiaozi.entity.ExamQuestion;

import java.util.List;

public interface AdminService {

    AdminLoginResultDTO login(String username, String password);

    PageResult<ExamQuestion> queryQuestions(Long categoryId, Long subjectId, Long chapterId, Long tagId,
                                            Integer type, Integer difficulty, Integer status, String keyword,
                                            int page, int pageSize);

    ExamQuestion saveQuestion(QuestionSaveDTO dto);

    void deleteQuestion(Long id);

    void batchDeleteQuestions(List<Long> ids);

    void batchUpdateStatus(List<Long> ids, Integer status);

    ExamCategory saveCategory(Long id, Long parentId, String type, String name, String icon,
                              String description, Integer sortOrder);

    void deleteCategory(Long id, String type);

    List<ExamCategory> getFullCategoryTree();
}
