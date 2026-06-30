package com.wxjiaozi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识点标签（MyBatis 原生版）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamTag {

    private Long id;
    private Long chapterId;
    private String name;
    private Integer questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
