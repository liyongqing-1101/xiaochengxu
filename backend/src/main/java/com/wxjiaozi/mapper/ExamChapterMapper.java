package com.wxjiaozi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wxjiaozi.entity.ExamChapter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamChapterMapper extends BaseMapper<ExamChapter> {

    @Select("SELECT * FROM exam_chapter WHERE subject_id = #{subjectId} ORDER BY sort_order ASC")
    List<ExamChapter> selectBySubjectId(@Param("subjectId") Long subjectId);
}
