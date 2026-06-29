package com.wxjiaozi.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wxjiaozi.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("user_check_in")
public class UserCheckIn extends BaseEntity {

    private Long userId;
    private LocalDate checkDate;
}
