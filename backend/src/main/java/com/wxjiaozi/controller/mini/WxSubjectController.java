package com.wxjiaozi.controller.mini;

import com.wxjiaozi.common.Result;
import com.wxjiaozi.dto.mini.SubjectDTO;
import com.wxjiaozi.security.CurrentUser;
import com.wxjiaozi.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小程序-首页科目接口
 */
@RestController
@RequestMapping("/wx/subject")
@Tag(name = "小程序-首页科目")
@RequiredArgsConstructor
public class WxSubjectController {

    private final RecordService recordService;

    /**
     * 获取首页科目列表（含用户已做题数/科目总题数）
     * <p>
     * 未登录用户也可访问，doneCount返回0。
     * </p>
     *
     * @param userId     当前登录用户（可选，未登录为null）
     * @param categoryId 分类ID
     * @return 科目列表，每个科目含 doneCount / totalQuestions
     */
    @GetMapping("/list")
    @Operation(summary = "首页科目列表（含已做题进度）")
    public Result<List<SubjectDTO>> getSubjectList(
            @CurrentUser(required = false) Long userId,
            @RequestParam Long categoryId) {
        List<SubjectDTO> list = recordService.getSubjectList(userId, categoryId);
        return Result.ok(list);
    }
}
