package com.wxjiaozi.controller.mini;

import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.common.Result;
import com.wxjiaozi.dto.mini.DailyQuestionDTO;
import com.wxjiaozi.dto.mini.EndSessionDTO;
import com.wxjiaozi.dto.mini.QuestionDTO;
import com.wxjiaozi.dto.mini.QuestionSessionDTO;
import com.wxjiaozi.dto.mini.SessionSummaryDTO;
import com.wxjiaozi.dto.mini.StartSessionDTO;
import com.wxjiaozi.dto.mini.SubmitAnswerDTO;
import com.wxjiaozi.dto.mini.SubmitResultDTO;
import com.wxjiaozi.security.CurrentUser;
import com.wxjiaozi.service.PracticeService;
import com.wxjiaozi.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/question")
@Tag(name = "小程序-题目")
@RequiredArgsConstructor
public class QuestionController {

    private final PracticeService practiceService;
    private final QuestionService questionService;

    @PostMapping("/session/start")
    @Operation(summary = "开始答题会话")
    public Result<QuestionSessionDTO> startSession(@CurrentUser Long userId,
                                                    @RequestBody @Valid StartSessionDTO startSessionDTO) {
        QuestionSessionDTO session = practiceService.startSession(userId, startSessionDTO);
        return Result.ok(session);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交答案")
    public Result<SubmitResultDTO> submitAnswer(@CurrentUser Long userId,
                                                 @RequestBody @Valid SubmitAnswerDTO submitAnswerDTO) {
        SubmitResultDTO result = practiceService.submitAnswer(userId, submitAnswerDTO);
        return Result.ok(result);
    }

    @PostMapping("/session/end")
    @Operation(summary = "结束答题会话")
    public Result<SessionSummaryDTO> endSession(@CurrentUser Long userId,
                                                 @RequestBody @Valid EndSessionDTO endSessionDTO) {
        SessionSummaryDTO summary = practiceService.endSession(userId, endSessionDTO);
        return Result.ok(summary);
    }

    @GetMapping("/list")
    @Operation(summary = "题目列表(分页+筛选)")
    public Result<PageResult<QuestionDTO>> listQuestions(
            @CurrentUser(required = false) Long userId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long chapterId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<QuestionDTO> result = questionService.getQuestionList(
                categoryId, subjectId, chapterId, tagId, type, difficulty, keyword, page, pageSize);
        return Result.ok(result);
    }

    @PostMapping("/collect")
    @Operation(summary = "收藏题目")
    public Result<Void> collectQuestion(@CurrentUser Long userId,
                                         @RequestBody Map<String, Long> body) {
        Long questionId = body.get("questionId");
        questionService.collectQuestion(userId, questionId);
        return Result.ok();
    }

    @DeleteMapping("/collect")
    @Operation(summary = "取消收藏题目")
    public Result<Void> uncollectQuestion(@CurrentUser Long userId,
                                           @RequestParam Long questionId) {
        questionService.uncollectQuestion(userId, questionId);
        return Result.ok();
    }

    @GetMapping("/collected")
    @Operation(summary = "已收藏题目列表")
    public Result<PageResult<QuestionDTO>> getCollectedQuestions(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<QuestionDTO> result = questionService.getCollectedQuestions(userId, page, pageSize);
        return Result.ok(result);
    }

    @GetMapping("/daily")
    @Operation(summary = "每日一题")
    public Result<DailyQuestionDTO> getDailyQuestion(@RequestParam Long categoryId) {
        DailyQuestionDTO question = questionService.getDailyQuestion(categoryId);
        return Result.ok(question);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索题目")
    public Result<PageResult<QuestionDTO>> searchQuestions(
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<QuestionDTO> result = questionService.searchQuestions(keyword, categoryId, page, pageSize);
        return Result.ok(result);
    }
}
