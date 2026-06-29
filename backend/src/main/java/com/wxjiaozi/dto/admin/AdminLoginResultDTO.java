package com.wxjiaozi.dto.admin;

import lombok.Data;

@Data
public class AdminLoginResultDTO {

    private String token;
    private String nickname;
    private String role;
}
