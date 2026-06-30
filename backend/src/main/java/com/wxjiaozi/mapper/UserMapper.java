package com.wxjiaozi.mapper;

import com.wxjiaozi.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper（MyBatis 原生版）
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询
     */
    User selectById(@Param("id") Long id);

    /**
     * 根据OpenID查询
     */
    User selectByOpenid(@Param("openid") String openid);

    /**
     * 根据用户名查询
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 查询所有用户
     */
    List<User> selectAll();

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户
     */
    int updateById(User user);

    /**
     * 根据ID删除
     */
    int deleteById(@Param("id") Long id);
}
