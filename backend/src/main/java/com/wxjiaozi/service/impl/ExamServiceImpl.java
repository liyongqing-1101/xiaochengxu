package com.wxjiaozi.service.impl;

import com.wxjiaozi.entity.ExamCategory;
import com.wxjiaozi.entity.ExamChapter;
import com.wxjiaozi.entity.ExamSubject;
import com.wxjiaozi.entity.ExamTag;
import com.wxjiaozi.mapper.ExamCategoryMapper;
import com.wxjiaozi.mapper.ExamChapterMapper;
import com.wxjiaozi.mapper.ExamSubjectMapper;
import com.wxjiaozi.mapper.ExamTagMapper;
import com.wxjiaozi.service.ExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ExamServiceImpl implements ExamService {

    private final ExamCategoryMapper examCategoryMapper;
    private final ExamSubjectMapper examSubjectMapper;
    private final ExamChapterMapper examChapterMapper;
    private final ExamTagMapper examTagMapper;

    public ExamServiceImpl(ExamCategoryMapper examCategoryMapper,
                           ExamSubjectMapper examSubjectMapper,
                           ExamChapterMapper examChapterMapper,
                           ExamTagMapper examTagMapper) {
        this.examCategoryMapper = examCategoryMapper;
        this.examSubjectMapper = examSubjectMapper;
        this.examChapterMapper = examChapterMapper;
        this.examTagMapper = examTagMapper;
    }

    @Override
    public List<ExamCategory> getCategories() {
        List<ExamCategory> categories = examCategoryMapper.selectAll();
        for (ExamCategory category : categories) {
            List<ExamSubject> subjects = examSubjectMapper.selectByCategoryId(category.getId());
            category.setSubjects(subjects);
        }
        return categories;
    }

    @Override
    public List<ExamSubject> getSubjects(Long categoryId) {
        return examSubjectMapper.selectByCategoryId(categoryId);
    }

    @Override
    public List<ExamChapter> getChapters(Long categoryId, Long subjectId) {
        return examChapterMapper.selectBySubjectId(subjectId);
    }

    @Override
    public List<ExamTag> getKnowledgePoints(Long categoryId, Long subjectId, Long chapterId) {
        return examTagMapper.selectByChapterId(chapterId);
    }
}
