package com.wxjiaozi.service;

import com.wxjiaozi.dto.imports.ImportProgressDTO;
import com.wxjiaozi.entity.ImportTask;

import java.io.InputStream;
import java.util.List;

/**
 * 题库导入服务接口
 */
public interface ImportService {

    /**
     * 创建导入任务
     */
    ImportTask createImportTask(Long adminId, String fileName, Long fileSize, Long categoryId);

    /**
     * 异步执行导入 (由@Async方法调用)
     */
    void executeImport(Long taskId, InputStream inputStream, Long categoryId);

    /**
     * 查询导入进度 (先查Redis, 再查DB)
     */
    ImportProgressDTO getProgress(Long taskId);

    /**
     * 更新Redis进度
     */
    void updateProgress(Long taskId, int currentRow, int successCount, int failCount, String status);

    /**
     * 分页查询导入任务列表
     */
    List<ImportTask> getImportTasks(int page, int pageSize);

    /**
     * 下载错误明细文件
     */
    byte[] downloadErrorFile(Long taskId);
}
