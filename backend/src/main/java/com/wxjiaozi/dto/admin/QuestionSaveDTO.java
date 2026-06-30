package com.wxjiaozi.dto.admin;

import com.wxjiaozi.dto.QuestionOptionDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

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
     * 格式：[{"key":"A","value":"选项内容"},{"key":"E","value":"选项内容"}]
     */
    private List<QuestionOptionDTO> optionList;

    @NotBlank(message = "答案不能为空")
    private String answer;

    private String explanation;

    private Integer status;
}
