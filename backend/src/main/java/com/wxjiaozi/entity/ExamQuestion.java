package com.wxjiaozi.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wxjiaozi.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("exam_question")
public class ExamQuestion extends BaseEntity {

    private Long categoryId;
    private Long subjectId;
    private Long chapterId;
    private Long tagId;
    private Integer type;
    private Integer difficulty;
    private Integer status;
    private String stem;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;
    private String explanation;
    private String stemImages;
}
