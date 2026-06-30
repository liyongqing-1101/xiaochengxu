package com.wxjiaozi.mapper;

import com.wxjiaozi.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 管理员 Mapper（MyBatis 原生版）
 */
@Mapper
public interface AdminUserMapper {

    AdminUser selectById(@Param("id") Long id);

    AdminUser selectByUsername(@Param("username") String username);

    List<AdminUser> selectAll();

    int insert(AdminUser admin);

    int updateById(AdminUser admin);

    int deleteById(@Param("id") Long id);
}
