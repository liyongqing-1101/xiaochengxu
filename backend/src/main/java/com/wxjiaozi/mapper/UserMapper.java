package com.wxjiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxjiaozi.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM user WHERE openid = #{openid}")
    User selectByOpenid(@Param("openid") String openid);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);
}
