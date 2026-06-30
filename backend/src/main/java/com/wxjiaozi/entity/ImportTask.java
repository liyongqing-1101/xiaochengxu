package com.wxjiaozi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 导入任务（MyBatis 原生版）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportTask {

    private Long id;
    private Long adminId;
    private String fileName;
    private Long fileSize;
    private Long categoryId;
    private Integer totalRows;
    private Integer successCount;
    private Integer failCount;
    private String status;
    private String errorFileUrl;
    private String errorDetail;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
