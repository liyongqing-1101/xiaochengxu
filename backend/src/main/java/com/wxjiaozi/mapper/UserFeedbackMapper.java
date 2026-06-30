package com.wxjiaozi.mapper;

import com.wxjiaozi.entity.UserFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户反馈 Mapper（MyBatis 原生版）
 */
@Mapper
public interface UserFeedbackMapper {

    UserFeedback selectById(@Param("id") Long id);

    List<UserFeedback> selectByUserId(@Param("userId") Long userId);

    List<UserFeedback> selectAll();

    int insert(UserFeedback feedback);

    int updateById(UserFeedback feedback);

    int deleteById(@Param("id") Long id);
}
