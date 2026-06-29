package com.wxjiaozi.service;

import com.wxjiaozi.entity.ExamCategory;
import com.wxjiaozi.entity.ExamChapter;
import com.wxjiaozi.entity.ExamSubject;
import com.wxjiaozi.entity.ExamTag;

import java.util.List;

public interface ExamService {

    List<ExamCategory> getCategories();

    List<ExamSubject> getSubjects(Long categoryId);

    List<ExamChapter> getChapters(Long categoryId, Long subjectId);

    List<ExamTag> getKnowledgePoints(Long categoryId, Long subjectId, Long chapterId);
}
