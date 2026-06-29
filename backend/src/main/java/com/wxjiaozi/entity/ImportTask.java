package com.wxjiaozi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wxjiaozi.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("import_task")
public class ImportTask extends BaseEntity {

    private Long adminId;
    private Long categoryId;
    private Long fileSize;
    private String fileName;
    private String status;

    @TableField("error_file_url")
    private String errorFileUrl;

    private String errorDetail;
    private Integer totalRows;
    private Integer successCount;
    private Integer failCount;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
