package com.wxjiaozi.mapper;

import com.wxjiaozi.entity.UserWrongQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 错题本 Mapper（MyBatis 原生版）
 */
@Mapper
public interface UserWrongQuestionMapper {

    /**
     * 根据ID查询
     */
    UserWrongQuestion selectById(@Param("id") Long id);

    /**
     * 根据用户ID分页查询错题列表
     */
    List<UserWrongQuestion> selectByUserIdWithPagination(
            @Param("userId") Long userId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 根据用户ID统计错题数量
     */
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和题目ID查询错题
     */
    UserWrongQuestion selectByUserIdAndQuestionId(
            @Param("userId") Long userId,
            @Param("questionId") Long questionId
    );

    /**
     * 插入错题记录
     */
    int insert(UserWrongQuestion record);

    /**
     * 更新错题记录
     */
    int updateById(UserWrongQuestion record);

    /**
     * 根据ID删除
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据用户ID和题目ID删除
     */
    int deleteByUserIdAndQuestionId(
            @Param("userId") Long userId,
            @Param("questionId") Long questionId
    );
}
