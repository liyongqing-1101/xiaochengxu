package com.wxjiaozi.mapper;

import com.wxjiaozi.entity.ExamSubject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 科目 Mapper（MyBatis 原生版）
 */
@Mapper
public interface ExamSubjectMapper {

    ExamSubject selectById(@Param("id") Long id);

    List<ExamSubject> selectByCategoryId(@Param("categoryId") Long categoryId);

    List<ExamSubject> selectAll();

    int insert(ExamSubject subject);

    int updateById(ExamSubject subject);

    int deleteById(@Param("id") Long id);

    /**
     * 科目题目计数+1
     */
    int incrementQuestionCount(@Param("subjectId") Long subjectId);

    /**
     * 科目题目计数-1
     */
    int decrementQuestionCount(@Param("subjectId") Long subjectId);
}
