package com.wxjiaozi.mapper;

import com.wxjiaozi.entity.UserCollection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户收藏 Mapper（MyBatis 原生版）
 */
@Mapper
public interface UserCollectionMapper {

    /**
     * 根据ID查询
     */
    UserCollection selectById(@Param("id") Long id);

    /**
     * 根据用户ID和题目ID查询
     */
    UserCollection selectByUserIdAndQuestionId(
            @Param("userId") Long userId,
            @Param("questionId") Long questionId
    );

    /**
     * 根据用户ID分页查询收藏列表
     */
    List<UserCollection> selectByUserIdWithPage(
            @Param("userId") Long userId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 根据用户ID统计收藏数量
     */
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 插入收藏记录
     */
    int insert(UserCollection record);

    /**
     * 根据ID删除
     */
    int deleteById(@Param("id") Long id);
}
