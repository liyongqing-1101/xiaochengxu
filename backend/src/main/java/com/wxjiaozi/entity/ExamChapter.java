package com.wxjiaozi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 章节（MyBatis 原生版）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamChapter {

    private Long id;
    private Long subjectId;
    private String name;
    private Integer questionCount;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 非数据库字段：知识点标签列表
     */
    private List<ExamTag> tags;
}
