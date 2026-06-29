package com.wxjiaozi.controller.admin;

import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.common.Result;
import com.wxjiaozi.dto.admin.BatchOperationDTO;
import com.wxjiaozi.dto.admin.QuestionSaveDTO;
import com.wxjiaozi.entity.ExamQuestion;
import com.wxjiaozi.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/questions")
@Tag(name = "管理后台-题库管理")
@RequiredArgsConstructor
public class AdminQuestionController {

    private final AdminService adminService;

    @GetMapping("/list")
    @Operation(summary = "题库列表(分页+筛选)")
    public Result<PageResult<ExamQuestion>> listQuestions(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<ExamQuestion> result = adminService.queryQuestions(
                categoryId, subjectId, chapterId, tagId, type, difficulty, status, keyword, page, pageSize);
        return Result.ok(result);
    }

    @PostMapping("/save")
    @Operation(summary = "新增/编辑题目")
    public Result<Void> saveQuestion(@RequestBody @Valid QuestionSaveDTO dto) {
        adminService.saveQuestion(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除题目")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        adminService.deleteQuestion(id);
        return Result.ok();
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除题目")
    public Result<Void> batchDeleteQuestions(@RequestBody @Valid BatchOperationDTO dto) {
        adminService.batchDeleteQuestions(dto.getIds());
        return Result.ok();
    }

    @PostMapping("/batch-status")
    @Operation(summary = "批量更新题目状态")
    public Result<Void> batchUpdateStatus(@RequestBody @Valid BatchOperationDTO dto) {
        adminService.batchUpdateStatus(dto.getIds(), dto.getStatus());
        return Result.ok();
    }
}
