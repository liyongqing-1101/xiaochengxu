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
            @Param("subjectId") Long subjectId,
            @Param("type") Integer type,
            @Param("status") Integer status,
            @Param("keyword") String keyword
    );

    /**
     * 按科目和题型统计题量（过滤禁用状态 status=0）
     * @param subjectId 科目ID
     * @return [{type: 1, count: 150}, {type: 2, count: 80}, ...]
     */
    List<Map<String, Object>> countBySubjectAndType(@Param("subjectId") Long subjectId);

    /**
     * 随机抽题：按科目+题型随机抽取指定数量的题目（过滤禁用状态 status=0）
     * @param subjectId 科目ID
     * @param type 题型，null 表示不限题型
     * @param limit 抽取数量
     * @return 随机题目列表
     */
    List<ExamQuestion> selectRandomBySubjectAndType(
            @Param("subjectId") Long subjectId,
            @Param("type") Integer type,
            @Param("limit") Integer limit
    );
}
