package com.wxjiaozi.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wxjiaozi.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("user")
public class User extends BaseEntity {

    private String openid;
    private String nickname;
    private String avatar;
    private String phone;
    private String membership;
    private Integer gender;
    private Integer status;
    private String username;
    @JsonIgnore
    private String password;
}
