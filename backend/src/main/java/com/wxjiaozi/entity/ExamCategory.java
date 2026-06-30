package com.wxjiaozi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试分类（MyBatis 原生版）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamCategory {

    private Long id;
    private String name;
    private String icon;
    private String description;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 非数据库字段：科目列表
     */
    private List<ExamSubject> subjects;
}
