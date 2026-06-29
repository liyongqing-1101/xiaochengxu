package com.wxjiaozi.controller.admin;

import com.wxjiaozi.common.BusinessException;
import com.wxjiaozi.common.Result;
import com.wxjiaozi.dto.imports.ImportProgressDTO;
import com.wxjiaozi.entity.ImportTask;
import com.wxjiaozi.security.CurrentUser;
import com.wxjiaozi.service.ImportService;
import com.wxjiaozi.util.ExcelExportUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/import")
@Tag(name = "管理后台-题库导入")
@RequiredArgsConstructor
public class AdminImportController {

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    private final ImportService importService;

    @GetMapping("/template")
    @Operation(summary = "下载导入模板")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] templateBytes = ExcelExportUtil.generateTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("题库导入模板.xlsx", StandardCharsets.UTF_8)
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(templateBytes);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传题库文件并导入")
    public Result<Map<String, Long>> uploadFile(@RequestParam MultipartFile file,
                                                 @RequestParam Long categoryId,
                                                 @CurrentUser("adminId") Long adminId) {
        // Validate file not empty
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            throw new BusinessException(400, "仅支持 .xlsx 或 .xls 格式的文件");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(400, "文件大小不能超过100MB");
        }

        // Create import task
        ImportTask task = importService.createImportTask(adminId, originalFilename, file.getSize(), categoryId);

        // Execute import asynchronously
        try {
            importService.executeImport(task.getId(), file.getInputStream(), categoryId);
        } catch (IOException e) {
            throw new BusinessException(500, "读取文件失败: " + e.getMessage());
        }

        Map<String, Long> data = new HashMap<>();
        data.put("taskId", task.getId());
        return Result.ok(data);
    }

    @GetMapping("/progress/{taskId}")
    @Operation(summary = "查询导入进度")
    public Result<ImportProgressDTO> getProgress(@PathVariable Long taskId) {
        ImportProgressDTO progress = importService.getProgress(taskId);
        return Result.ok(progress);
    }

    @GetMapping("/tasks")
    @Operation(summary = "导入任务列表")
    public Result<List<ImportTask>> getTasks(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int pageSize) {
        List<ImportTask> tasks = importService.getImportTasks(page, pageSize);
        return Result.ok(tasks);
    }

    @GetMapping("/error-file/{taskId}")
    @Operation(summary = "下载导入失败明细文件")
    public ResponseEntity<byte[]> downloadErrorFile(@PathVariable Long taskId) {
        byte[] errorBytes = importService.downloadErrorFile(taskId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("导入失败明细_" + taskId + ".xlsx", StandardCharsets.UTF_8)
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(errorBytes);
    }
}
