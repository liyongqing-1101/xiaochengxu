package com.wxjiaozi.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 题目保存DTO（简化版）
 * 移除难度、章节、知识点相关字段
 */
@Data
public class QuestionSaveDTO {

    private Long id;

    @NotNull(message = "科目ID不能为空")
    private Long subjectId;

    @NotNull(message = "题目类型不能为空")
    private Integer type;

    @NotBlank(message = "题干不能为空")
    private String stem;

    /**
     * 题目选项数组（JSON格式，单选/多选题使用，判断题为null）
     * 简化格式：["选项A内容","选项B内容","选项C内容","选项D内容"]
     */
    private List<String> optionList;

    @NotBlank(message = "答案不能为空")
    private String answer;

    private String explanation;

    private Integer status;
}
