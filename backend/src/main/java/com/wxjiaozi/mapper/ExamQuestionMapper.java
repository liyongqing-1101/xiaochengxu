package com.wxjiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxjiaozi.entity.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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

    /**
     * 按科目和题型统计题目数量
     * @return [{type: 1, count: 150}, {type: 2, count: 80}, ...]
     */
    List<Map<String, Object>> countBySubjectAndType(@Param("subjectId") Long subjectId);
}
