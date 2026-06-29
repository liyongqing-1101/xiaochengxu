package com.wxjiaozi.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wxjiaozi.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("user_wrong_question")
public class UserWrongQuestion extends BaseEntity {

    private Long userId;
    private Long questionId;
    private Integer errorCount;
    private Integer source;
    private String lastWrongAnswer;
}
