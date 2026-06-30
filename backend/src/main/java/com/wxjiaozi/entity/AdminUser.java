package com.wxjiaozi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员（MyBatis 原生版）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {

    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private String nickname;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
