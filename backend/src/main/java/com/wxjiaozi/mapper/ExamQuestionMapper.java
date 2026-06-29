package com.wxjiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxjiaozi.entity.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {

    Page<ExamQuestion> selectPageWithFilters(
            Page<ExamQuestion> page,
            @Param("categoryId") Long categoryId,
            @Param("subjectId") Long subjectId,
            @Param("chapterId") Long chapterId,
            @Param("tagId") Long tagId,
            @Param("type") Integer type,
            @Param("difficulty") Integer difficulty,
            @Param("status") Integer status,
            @Param("keyword") String keyword
    );

    int insertBatch(@Param("list") List<ExamQuestion> list);
}
