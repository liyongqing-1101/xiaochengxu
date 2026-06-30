package com.wxjiaozi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 错题本（MyBatis 原生版）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWrongQuestion {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 错误次数
     */
    private Integer errorCount;

    /**
     * 来源：1=练习，2=考试，3=每日一题
     */
    private Integer source;

    /**
     * 最后一次错误答案
     */
    private String lastWrongAnswer;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
