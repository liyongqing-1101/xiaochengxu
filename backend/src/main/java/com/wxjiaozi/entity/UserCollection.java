package com.wxjiaozi.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wxjiaozi.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("user_collection")
public class UserCollection extends BaseEntity {

    private Long userId;
    private Long questionId;
}
