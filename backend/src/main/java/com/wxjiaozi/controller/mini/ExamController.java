package com.wxjiaozi.controller.mini;

import com.wxjiaozi.common.Result;
import com.wxjiaozi.entity.ExamCategory;
import com.wxjiaozi.entity.ExamChapter;
import com.wxjiaozi.entity.ExamSubject;
import com.wxjiaozi.entity.ExamTag;
import com.wxjiaozi.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wx/exam")
@Tag(name = "小程序-考试分类")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping("/categories")
    @Operation(summary = "获取考试分类列表")
    public Result<List<ExamCategory>> getCategories() {
        List<ExamCategory> categories = examService.getCategories();
        return Result.ok(categories);
    }

    @GetMapping("/subjects")
    @Operation(summary = "获取科目列表")
    public Result<List<ExamSubject>> getSubjects(@RequestParam Long categoryId) {
        List<ExamSubject> subjects = examService.getSubjects(categoryId);
        return Result.ok(subjects);
    }

    @GetMapping("/chapters")
    @Operation(summary = "获取章节列表")
    public Result<List<ExamChapter>> getChapters(@RequestParam Long categoryId,
                                                  @RequestParam Long subjectId) {
        List<ExamChapter> chapters = examService.getChapters(categoryId, subjectId);
        return Result.ok(chapters);
    }

    @GetMapping("/knowledge-points")
    @Operation(summary = "获取知识点列表")
    public Result<List<ExamTag>> getKnowledgePoints(@RequestParam Long categoryId,
                                                     @RequestParam Long subjectId,
                                                     @RequestParam Long chapterId) {
        List<ExamTag> tags = examService.getKnowledgePoints(categoryId, subjectId, chapterId);
        return Result.ok(tags);
    }
}
