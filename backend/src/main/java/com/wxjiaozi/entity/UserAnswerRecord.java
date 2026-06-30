package com.wxjiaozi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户答题记录（MyBatis 原生版）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerRecord {

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
     * 科目ID
     */
    private Long subjectId;

    /**
     * 答题会话ID
     */
    private String sessionId;

    /**
     * 用户选择的选项（逗号分隔）
     */
    private String selectedOptions;

    /**
     * 是否正确：0=错误，1=正确
     */
    private Integer isCorrect;

    /**
     * 答题耗时（秒）
     */
    private Integer duration;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
