package com.wxjiaozi.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体（MyBatis 原生版）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 微信OpenID
     */
    private String openid;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 会员类型：free/vip
     */
    private String membership;

    /**
     * 性别：0=未知，1=男，2=女
     */
    private Integer gender;

    /**
     * 状态：0=禁用，1=正常
     */
    private Integer status;

    /**
     * 登录用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    @JsonIgnore
    private String password;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
