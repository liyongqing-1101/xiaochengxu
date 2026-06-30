package com.wxjiaozi.mapper;

import com.wxjiaozi.entity.ExamChapter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 章节 Mapper（MyBatis 原生版）
 */
@Mapper
public interface ExamChapterMapper {

    ExamChapter selectById(@Param("id") Long id);

    List<ExamChapter> selectBySubjectId(@Param("subjectId") Long subjectId);

    int insert(ExamChapter chapter);

    int updateById(ExamChapter chapter);

    int deleteById(@Param("id") Long id);
}
