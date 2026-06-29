package com.wxjiaozi.dto.mini;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {

    @NotBlank(message = "微信code不能为空")
    private String code;
}
