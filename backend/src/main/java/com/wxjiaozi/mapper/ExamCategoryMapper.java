package com.wxjiaozi.mapper;

import com.wxjiaozi.entity.ExamCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考试分类 Mapper（MyBatis 原生版）
 */
@Mapper
public interface ExamCategoryMapper {

    ExamCategory selectById(@Param("id") Long id);

    List<ExamCategory> selectAll();

    int insert(ExamCategory category);

    int updateById(ExamCategory category);

    int deleteById(@Param("id") Long id);
}
