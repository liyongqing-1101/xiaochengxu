package com.wxjiaozi.mapper;

import com.wxjiaozi.entity.ExamTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识点标签 Mapper（MyBatis 原生版）
 */
@Mapper
public interface ExamTagMapper {

    ExamTag selectById(@Param("id") Long id);

    List<ExamTag> selectByChapterId(@Param("chapterId") Long chapterId);

    int insert(ExamTag tag);

    int updateById(ExamTag tag);

    int deleteById(@Param("id") Long id);
}
