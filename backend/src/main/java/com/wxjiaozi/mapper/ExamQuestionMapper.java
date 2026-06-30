package com.wxjiaozi.mapper;

import com.wxjiaozi.entity.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 题目 Mapper（MyBatis 原生版）
 */
@Mapper
public interface ExamQuestionMapper {

    /**
     * 根据ID查询题目
     * @param id 题目ID
     * @return 题目对象
     */
    ExamQuestion selectById(@Param("id") Long id);

    /**
     * 分页查询题目列表（支持多条件筛选）
     * @param offset 偏移量
     * @param limit 每页数量
     * @param subjectId 科目ID（可选）
     * @param type 题型（可选）
     * @param status 状态（可选）
     * @param keyword 题干关键词（可选）
     * @return 题目列表
     */
    List<ExamQuestion> selectPageWithFilters(
            @Param("offset") Integer offset,
            @Param("limit") Integer limit,
            @Param("subjectId") Long subjectId,
            @Param("type") Integer type,
            @Param("status") Integer status,
            @Param("keyword") String keyword
    );

    /**
     * 统计符合条件的题目总数
     * @param subjectId 科目ID（可选）
     * @param type 题型（可选）
     * @param status 状态（可选）
     * @param keyword 题干关键词（可选）
     * @return 总数
     */
    Long countWithFilters(
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

    /**
     * 插入新题目
     * @param question 题目对象
     * @return 影响行数
     */
    int insert(ExamQuestion question);

    /**
     * 批量插入题目
     * @param list 题目列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<ExamQuestion> list);

    /**
     * 更新题目
     * @param question 题目对象
     * @return 影响行数
     */
    int updateById(ExamQuestion question);

    /**
     * 根据ID删除题目
     * @param id 题目ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除题目
     * @param ids ID列表
     * @return 影响行数
     */
    int batchDeleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据ID列表批量查询题目
     * @param ids ID列表
     * @return 题目列表
     */
    List<ExamQuestion> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 查询所有正常状态的题目
     * @return 题目列表
     */
    List<ExamQuestion> selectAllActive();

    /**
     * 根据偏移量查询单条题目（用于每日一题）
     * @param offset 偏移量
     * @return 题目
     */
    ExamQuestion selectByOffset(@Param("offset") Integer offset);

    /**
     * 查询所有题目（仅返回题干字段用于去重）
     * @return 题目列表（仅包含stem字段）
     */
    List<ExamQuestion> selectAllForDeduplication();

    /**
     * 统计指定科目和状态的题目数量
     * @param subjectId 科目ID
     * @param status 状态
     * @return 总数
     */
    Long countBySubjectAndStatus(
            @Param("subjectId") Long subjectId,
            @Param("status") Integer status
    );
}
