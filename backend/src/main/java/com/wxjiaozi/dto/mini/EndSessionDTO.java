package com.wxjiaozi.dto.mini;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EndSessionDTO {

    @NotBlank(message = "会话ID不能为空")
    private String sessionId;
}
