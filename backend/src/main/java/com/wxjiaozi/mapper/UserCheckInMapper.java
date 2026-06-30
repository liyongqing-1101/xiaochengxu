package com.wxjiaozi.mapper;

import com.wxjiaozi.entity.UserCheckIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户打卡 Mapper（MyBatis 原生版）
 */
@Mapper
public interface UserCheckInMapper {

    UserCheckIn selectById(@Param("id") Long id);

    UserCheckIn selectByUserIdAndDate(@Param("userId") Long userId, @Param("checkDate") LocalDate checkDate);

    List<UserCheckIn> selectByUserId(@Param("userId") Long userId);

    int countByUserId(@Param("userId") Long userId);

    UserCheckIn selectLatestByUserId(@Param("userId") Long userId);

    int insert(UserCheckIn record);

    int deleteById(@Param("id") Long id);
}
