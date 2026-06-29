package com.wxjiaozi.dto.mini;

import lombok.Data;

@Data
public class LoginResultDTO {

    private String token;
    private String refreshToken;
    private UserInfoDTO userInfo;

    @Data
    public static class UserInfoDTO {
        private Long id;
        private String nickname;
        private String avatar;
        private Integer gender;
        private String phone;
        private String membership;
    }
}
