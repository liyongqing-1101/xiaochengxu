package com.wxjiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wxjiaozi.entity.UserCollection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserCollectionMapper extends BaseMapper<UserCollection> {

    @Select("SELECT * FROM user_collection WHERE user_id = #{userId} AND question_id = #{questionId}")
    UserCollection selectByUserIdAndQuestionId(@Param("userId") Long userId, @Param("questionId") Long questionId);

    @Select("SELECT * FROM user_collection WHERE user_id = #{userId} ORDER BY created_at DESC")
    Page<UserCollection> selectPageByUserId(Page<UserCollection> page, @Param("userId") Long userId);
}
