package com.wxjiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxjiaozi.entity.UserWrongQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserWrongQuestionMapper extends BaseMapper<UserWrongQuestion> {

    @Select("SELECT * FROM user_wrong_question WHERE user_id = #{userId} ORDER BY updated_at DESC")
    List<UserWrongQuestion> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM user_wrong_question WHERE user_id = #{userId} AND question_id = #{questionId}")
    UserWrongQuestion selectByUserIdAndQuestionId(@Param("userId") Long userId, @Param("questionId") Long questionId);
}
