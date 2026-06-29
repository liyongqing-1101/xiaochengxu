package com.wxjiaozi.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionSaveDTO {

    private Long id;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotNull(message = "科目ID不能为空")
    private Long subjectId;

    private Long chapterId;

    private Long tagId;

    @NotNull(message = "题目类型不能为空")
    private Integer type;

    @NotBlank(message = "题干不能为空")
    private String stem;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;

    @NotBlank(message = "答案不能为空")
    private String answer;

    private String explanation;

    private Integer difficulty;

    private Integer status;
}
