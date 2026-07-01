package com.wxjiaozi.controller.mini;

import com.wxjiaozi.common.Result;
import com.wxjiaozi.dto.mini.SubmitAnswerDTO;
import com.wxjiaozi.dto.mini.SubmitResultDTO;
import com.wxjiaozi.security.CurrentUser;
import com.wxjiaozi.service.RecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序-做题记录接口
 */
@RestController
@RequestMapping("/wx/record")
@Tag(name = "小程序-做题记录")
@RequiredArgsConstructor
public class WxRecordController {

    private final RecordService recordService;

    /**
     * 提交答题结果
     * <p>
     * 需要登录。同一用户同一题目重复刷题不新增记录，doneCount只算1次。
     * </p>
     *
     * @param userId 当前登录用户
     * @param params 提交参数
     * @return 答题结果（是否正确、正确答案、解析）
     */
    @PostMapping("/submit")
    @Operation(summary = "提交答题结果")
    public Result<SubmitResultDTO> submitAnswer(@CurrentUser Long userId,
                                                 @RequestBody @Valid SubmitAnswerDTO params) {
        SubmitResultDTO result = recordService.submitAnswer(userId, params);
        return Result.ok(result);
    }

    /**
     * 删除做题记录
     * <p>
     * 同步扣减Redis已做题计数。
     * </p>
     *
     * @param userId     当前登录用户
     * @param questionId 题目ID
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除做题记录")
    public Result<Void> deleteRecord(@CurrentUser Long userId,
                                      @RequestParam Long questionId) {
        recordService.deleteRecord(userId, questionId);
        return Result.ok();
    }
}
