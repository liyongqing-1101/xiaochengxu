package com.wxjiaozi.dto.imports;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 导入结果 DTO
 */
@Data
public class ImportResultDTO {
    private Long taskId;
    private String fileName;
    private Integer totalRows;
    private Integer successCount;
    private Integer failCount;
    private String status;
    private String errorFileUrl;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
