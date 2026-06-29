package com.wxjiaozi.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wxjiaozi.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("admin_user")
public class AdminUser extends BaseEntity {

    private String username;
    private String password;
    private String nickname;
    private String role;
    private Integer status;
}
