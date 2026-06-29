package com.wxjiaozi.controller.admin;

import com.wxjiaozi.common.Result;
import com.wxjiaozi.entity.ExamCategory;
import com.wxjiaozi.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/categories")
@Tag(name = "管理后台-分类管理")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminService adminService;

    @GetMapping("/tree")
    @Operation(summary = "获取完整分类树")
    public Result<List<ExamCategory>> getCategoryTree() {
        List<ExamCategory> tree = adminService.getFullCategoryTree();
        return Result.ok(tree);
    }

    @PostMapping("/save")
    @Operation(summary = "新增/编辑分类")
    public Result<Void> saveCategory(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") != null ? ((Number) body.get("id")).longValue() : null;
        Long parentId = body.get("parentId") != null ? ((Number) body.get("parentId")).longValue() : null;
        String type = (String) body.get("type");
        String name = (String) body.get("name");
        String icon = (String) body.get("icon");
        String description = (String) body.get("description");
        Integer sortOrder = body.get("sortOrder") != null
                ? ((Number) body.get("sortOrder")).intValue() : null;

        adminService.saveCategory(id, parentId, type, name, icon, description, sortOrder);
        return Result.ok();
    }

    @DeleteMapping("/{type}/{id}")
    @Operation(summary = "删除分类")
    public Result<Void> deleteCategory(@PathVariable String type,
                                        @PathVariable Long id) {
        adminService.deleteCategory(id, type);
        return Result.ok();
    }
}
