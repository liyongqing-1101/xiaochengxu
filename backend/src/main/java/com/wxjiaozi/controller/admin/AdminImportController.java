package com.wxjiaozi.controller.admin;

import com.wxjiaozi.common.Result;
import com.wxjiaozi.dto.admin.QuestionSaveDTO;
import com.wxjiaozi.service.AdminService;
import com.wxjiaozi.util.ExcelImportUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 管理后台 - Excel导入控制器
 * 简化版：支持6列模板导入
 */
@Slf4j
@RestController
@RequestMapping("/admin/import")
@Tag(name = "管理后台-Excel导入")
@RequiredArgsConstructor
public class AdminImportController {

    private final AdminService adminService;

    @PostMapping("/upload")
    @Operation(summary = "上传Excel导入题目")
    public Result<ImportResult> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择上传文件");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return Result.fail("仅支持 .xlsx 或 .xls 格式的Excel文件");
        }

        try {
            ExcelImportUtil.ImportParseResult parseResult = ExcelImportUtil.parseExcel(file.getInputStream());

            int successCount = 0;
            List<ExcelImportUtil.ImportErrorRow> errors = new ArrayList<>();

            // 批量保存题目
            for (int i = 0; i < parseResult.getRows().size(); i++) {
                ExcelImportUtil.ImportQuestionRow row = parseResult.getRows().get(i);
                try {
                    QuestionSaveDTO dto = convertToDTO(row);
                    adminService.saveQuestion(dto);
                    successCount++;
                } catch (Exception e) {
                    errors.add(new ExcelImportUtil.ImportErrorRow(
                            row.getRowNum(),
                            row.getStem(),
                            e.getMessage()
                    ));
                }
            }

            ImportResult result = new ImportResult();
            result.setTotal(parseResult.getRows().size());
            result.setSuccess(successCount);
            result.setFailed(errors.size());
            result.setErrors(errors);

            return Result.ok(result);
        } catch (Exception e) {
            log.error("导入失败", e);
            return Result.fail("导入失败：" + e.getMessage());
        }
    }

    /**
     * 将导入行转换为保存DTO
     */
    private QuestionSaveDTO convertToDTO(ExcelImportUtil.ImportQuestionRow row) {
        QuestionSaveDTO dto = new QuestionSaveDTO();
        dto.setSubjectId(row.getSubjectId() != null ? row.getSubjectId() : 1);
        dto.setType(row.getType());
        dto.setStem(row.getStem());
        dto.setAnswer(row.getAnswer());
        dto.setExplanation(row.getExplanation());
        dto.setStatus(1); // 默认上架

        // 处理选项（用分号分隔）
        if (row.getOptions() != null && !row.getOptions().trim().isEmpty()) {
            String[] optionArray = row.getOptions().split(";");
            List<String> optionList = new ArrayList<>();
            for (String opt : optionArray) {
                if (opt != null && !opt.trim().isEmpty()) {
                    optionList.add(opt.trim());
                }
            }
            dto.setOptionList(optionList);
        }

        return dto;
    }

    /**
     * 导入结果
     */
    @lombok.Data
    public static class ImportResult {
        private int total;
        private int success;
        private int failed;
        private List<ExcelImportUtil.ImportErrorRow> errors;
    }
}
