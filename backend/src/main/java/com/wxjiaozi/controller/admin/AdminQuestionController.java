package com.wxjiaozi.controller.admin;

import com.wxjiaozi.common.BusinessException;
import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.common.Result;
import com.wxjiaozi.dto.admin.QuestionSaveDTO;
import com.wxjiaozi.entity.ExamQuestion;
import com.wxjiaozi.service.AdminService;
import com.wxjiaozi.util.ExcelExportUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 管理后台 - 题库管理控制器
 * 简化版：仅保留表中实际存在的字段
 */
@Slf4j
@RestController
@RequestMapping("/admin/questions")
@Tag(name = "管理后台-题库管理")
@RequiredArgsConstructor
public class AdminQuestionController {

    private final AdminService adminService;

    @GetMapping("/list")
    @Operation(summary = "题库列表(分页+筛选)")
    public Result<PageResult<ExamQuestion>> listQuestions(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<ExamQuestion> result = adminService.queryQuestions(
                null, subjectId, null, null, type, null, status, keyword, page, pageSize);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取单题详情")
    public Result<ExamQuestion> getQuestion(@PathVariable Long id) {
        return Result.ok(adminService.getQuestionById(id));
    }

    @PostMapping("/save")
    @Operation(summary = "新增/编辑题目")
    public Result<ExamQuestion> saveQuestion(@RequestBody @Valid QuestionSaveDTO dto) {
        try {
            ExamQuestion question = adminService.saveQuestion(dto);
            return Result.ok(question);
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("保存题目异常", e);
            return Result.fail("服务器内部错误: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除题目")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        adminService.deleteQuestion(id);
        return Result.ok();
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除题目")
    public Result<Void> batchDeleteQuestions(@RequestBody List<Long> ids) {
        adminService.batchDeleteQuestions(ids);
        return Result.ok();
    }

    @PostMapping("/batch-status")
    @Operation(summary = "批量更新题目状态")
    public Result<Void> batchUpdateStatus(@RequestBody List<Long> ids, @RequestParam Integer status) {
        adminService.batchUpdateStatus(ids, status);
        return Result.ok();
    }

    @GetMapping("/template")
    @Operation(summary = "下载导入模板")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        // 1. 设置正确的响应头（关键：避免JSON转换器拦截）
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" +
                URLEncoder.encode("题库导入模板.xlsx", StandardCharsets.UTF_8));
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 2. 生成Excel模板
        byte[] excelBytes = ExcelExportUtil.generateTemplate();

        // 3. 直接写入输出流（完整关闭，避免损坏）
        try (OutputStream out = response.getOutputStream()) {
            out.write(excelBytes);
            out.flush();
        }
    }
}
