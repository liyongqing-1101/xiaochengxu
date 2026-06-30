package com.wxjiaozi.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wxjiaozi.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("user_answer_record")
public class UserAnswerRecord extends BaseEntity {

    private Long userId;
    private Long questionId;
    private Long subjectId;
    private String sessionId;
    private String selectedOptions;
    private Integer isCorrect;
    private Integer duration;
}
