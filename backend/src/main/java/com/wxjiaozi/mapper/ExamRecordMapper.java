package com.wxjiaozi.mapper;

import com.wxjiaozi.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户做题明细 Mapper（去重版）
 */
@Mapper
public interface ExamRecordMapper {

    /**
     * 插入做题记录（INSERT IGNORE，重复题目忽略不插入）
     *
     * @param record 做题记录
     * @return 影响行数（1=新插入，0=重复忽略）
     */
    int insertIgnore(ExamRecord record);

    /**
     * 插入或更新做题记录
     * 如果题目已做过则更新is_correct，否则插入新记录
     *
     * @param record 做题记录
     * @return 影响行数
     */
    int insertOrUpdate(ExamRecord record);

    /**
     * 根据用户ID和题目ID查询记录
     */
    ExamRecord selectByUserIdAndQuestionId(@Param("userId") Long userId,
                                           @Param("questionId") Long questionId);

    /**
     * 统计用户某科目下已做题总数（去重）
     *
     * @param userId    用户ID
     * @param subjectId 科目ID
     * @return 已做题数量
     */
    Long countByUserIdAndSubjectId(@Param("userId") Long userId,
                                   @Param("subjectId") Long subjectId);

    /**
     * 删除用户某条做题记录
     *
     * @param id 记录ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据用户ID和题目ID删除做题记录
     *
     * @param userId     用户ID
     * @param questionId 题目ID
     * @return 影响行数
     */
    int deleteByUserIdAndQuestionId(@Param("userId") Long userId,
                                    @Param("questionId") Long questionId);
}
