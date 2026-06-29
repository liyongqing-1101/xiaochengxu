package com.wxjiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxjiaozi.entity.UserCheckIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserCheckInMapper extends BaseMapper<UserCheckIn> {

    @Select("SELECT * FROM user_check_in WHERE user_id = #{userId} AND check_date = #{checkDate}")
    UserCheckIn selectByUserIdAndDate(@Param("userId") Long userId, @Param("checkDate") String checkDate);

    @Select("SELECT COUNT(*) FROM user_check_in WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM user_check_in WHERE user_id = #{userId} ORDER BY check_date DESC LIMIT 1")
    UserCheckIn selectLatestByUserId(@Param("userId") Long userId);
}
