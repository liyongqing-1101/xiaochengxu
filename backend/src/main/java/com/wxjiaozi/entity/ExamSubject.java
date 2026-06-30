package com.wxjiaozi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 科目（MyBatis 原生版）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubject {

    private Long id;
    private Long categoryId;
    private String name;
    private String icon;
    private Integer totalQuestions;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 非数据库字段：章节列表
     */
    private List<ExamChapter> chapters;
}
