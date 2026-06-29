package com.wxjiaozi.dto.mini;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmitAnswerDTO {

    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    @NotNull(message = "选中选项不能为空")
    private List<String> selectedOptions;

    private Integer duration;
}
