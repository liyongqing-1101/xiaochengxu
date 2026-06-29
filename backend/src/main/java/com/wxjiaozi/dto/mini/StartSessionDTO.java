package com.wxjiaozi.dto.mini;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartSessionDTO {

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotNull(message = "科目ID不能为空")
    private Long subjectId;

    private Long chapterId;

    private Long knowledgePointId;

    private Integer questionCount;

    private String mode;
}
