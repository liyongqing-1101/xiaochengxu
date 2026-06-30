package com.wxjiaozi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户反馈（MyBatis 原生版）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedback {

    private Long id;
    private Long userId;
    private String content;
    private String contact;
    private Integer status;
    private LocalDateTime createdAt;
}
