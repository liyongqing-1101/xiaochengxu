package com.wxjiaozi.controller.mini;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxjiaozi.common.BusinessException;
import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.common.Result;
import com.wxjiaozi.dto.mini.WrongQuestionDTO;
import com.wxjiaozi.dto.mini.WrongQuestionSyncDTO;
import com.wxjiaozi.security.CurrentUser;
import com.wxjiaozi.service.WrongBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wx/wrong-book")
@Tag(name = "小程序-错题本")
@RequiredArgsConstructor
public class WrongBookController {

    private final WrongBookService wrongBookService;
    private final ObjectMapper objectMapper;

    @PostMapping("/sync")
    @Operation(summary = "同步错题")
    public Result<Void> syncWrongQuestions(@CurrentUser Long userId,
                                            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Object> rawList = (List<Object>) body.get("questions");
        if (rawList == null) {
            throw new BusinessException(400, "questions参数不能为空");
        }
        List<WrongQuestionSyncDTO> questions = objectMapper.convertValue(
                rawList, new TypeReference<List<WrongQuestionSyncDTO>>() {});
        wrongBookService.syncWrongQuestions(userId, questions);
        return Result.ok();
    }

    @GetMapping("/list")
    @Operation(summary = "错题列表")
    public Result<PageResult<WrongQuestionDTO>> getWrongQuestions(
            @CurrentUser Long userId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<WrongQuestionDTO> result = wrongBookService.getWrongList(
                userId, subjectId, page, pageSize);
        return Result.ok(result);
    }

    @DeleteMapping("/remove")
    @Operation(summary = "移除错题")
    public Result<Void> removeWrongQuestion(@CurrentUser Long userId,
                                             @RequestParam Long questionId) {
        wrongBookService.removeWrong(userId, questionId);
        return Result.ok();
    }
}
