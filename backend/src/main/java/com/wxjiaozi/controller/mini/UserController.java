package com.wxjiaozi.controller.mini;

import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.common.Result;
import com.wxjiaozi.dto.mini.CheckInResultDTO;
import com.wxjiaozi.dto.mini.LoginResultDTO.UserInfoDTO;
import com.wxjiaozi.dto.mini.PracticeHistoryDTO;
import com.wxjiaozi.dto.mini.UserStatsDTO;
import com.wxjiaozi.security.CurrentUser;
import com.wxjiaozi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/wx/user")
@Tag(name = "小程序-用户")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    @Operation(summary = "获取用户信息")
    public Result<UserInfoDTO> getUserInfo(@CurrentUser Long userId) {
        UserInfoDTO info = userService.getUserInfo(userId);
        return Result.ok(info);
    }

    @PutMapping("/info")
    @Operation(summary = "更新用户信息")
    public Result<Void> updateUserInfo(@CurrentUser Long userId,
                                        @RequestBody Map<String, String> body) {
        String nickname = body.get("nickname");
        String avatar = body.get("avatar");
        userService.updateUserInfo(userId, nickname, avatar);
        return Result.ok();
    }

    @GetMapping("/stats")
    @Operation(summary = "获取用户学习统计")
    public Result<UserStatsDTO> getUserStats(@CurrentUser Long userId,
                                              @RequestParam Long categoryId) {
        UserStatsDTO stats = userService.getStats(userId, categoryId);
        return Result.ok(stats);
    }

    @PostMapping("/check-in")
    @Operation(summary = "每日签到")
    public Result<CheckInResultDTO> checkIn(@CurrentUser Long userId) {
        CheckInResultDTO result = userService.checkIn(userId);
        return Result.ok(result);
    }

    @GetMapping("/history")
    @Operation(summary = "答题历史记录")
    public Result<PageResult<PracticeHistoryDTO>> getHistory(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<PracticeHistoryDTO> history = userService.getHistory(userId, page, pageSize);
        return Result.ok(history);
    }

    @PostMapping("/feedback")
    @Operation(summary = "提交反馈")
    public Result<Void> submitFeedback(@CurrentUser Long userId,
                                        @RequestBody Map<String, String> body) {
        String content = body.get("content");
        String contact = body.get("contact");
        userService.submitFeedback(userId, content, contact);
        return Result.ok();
    }

    @PostMapping("/logout")
    @Operation(summary = "用户退出登录")
    public Result<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            userService.logout(token);
        }
        return Result.ok();
    }

    @PutMapping("/updateNickname")
    @Operation(summary = "更新用户昵称")
    public Result<Void> updateNickname(@CurrentUser Long userId,
                                        @RequestBody Map<String, String> body) {
        String nickname = body.get("nickname");
        if (nickname == null || nickname.trim().isEmpty()) {
            return Result.fail(400, "昵称不能为空");
        }
        userService.updateUserInfo(userId, nickname.trim(), null);
        return Result.ok();
    }
}
