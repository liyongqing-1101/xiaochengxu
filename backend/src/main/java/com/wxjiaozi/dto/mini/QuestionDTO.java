package com.wxjiaozi.dto.mini;

import lombok.Data;

@Data
public class QuestionDTO {

    private Long id;
    private Long categoryId;
    private Long subjectId;
    private Long chapterId;
    private Long tagId;
    private Integer type;
    private String stem;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;
    private String explanation;
    private Integer difficulty;
    private Integer status;
}
