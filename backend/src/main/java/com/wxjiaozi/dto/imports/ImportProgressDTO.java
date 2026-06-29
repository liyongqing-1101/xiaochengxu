package com.wxjiaozi.dto.imports;

import lombok.Data;
import java.util.List;

/**
 * 导入进度 DTO
 */
@Data
public class ImportProgressDTO {
    private Long taskId;
    private Integer totalRows;
    private Integer currentRow;
    private Integer successCount;
    private Integer failCount;
    private Integer percent;
    private String status;
    private List<String> errorMessages;
}
