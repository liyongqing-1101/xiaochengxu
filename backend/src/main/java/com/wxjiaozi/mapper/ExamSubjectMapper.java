package com.wxjiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxjiaozi.entity.ExamSubject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamSubjectMapper extends BaseMapper<ExamSubject> {

    @Select("SELECT * FROM exam_subject WHERE category_id = #{categoryId} ORDER BY sort_order ASC")
    List<ExamSubject> selectByCategoryId(@Param("categoryId") Long categoryId);
}
