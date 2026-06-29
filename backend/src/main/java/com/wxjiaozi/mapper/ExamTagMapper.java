package com.wxjiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxjiaozi.entity.ExamTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamTagMapper extends BaseMapper<ExamTag> {

    @Select("SELECT * FROM exam_tag WHERE chapter_id = #{chapterId} ORDER BY id ASC")
    List<ExamTag> selectByChapterId(@Param("chapterId") Long chapterId);
}
