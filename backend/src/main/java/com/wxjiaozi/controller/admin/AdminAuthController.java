package com.wxjiaozi.controller.admin;

import cn.hutool.crypto.digest.BCrypt;
import com.wxjiaozi.common.Result;
import com.wxjiaozi.dto.admin.AdminLoginDTO;
import com.wxjiaozi.entity.AdminUser;
import com.wxjiaozi.mapper.AdminUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/auth")
@Tag(name = "管理后台-认证")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminUserMapper adminUserMapper;

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<Map<String, Object>> login(@RequestBody @Valid AdminLoginDTO loginDTO, HttpSession session) {
        log.info("Admin login attempt: username={}", loginDTO.getUsername());

        // 1. 查询用户（原生MyBatis）
        AdminUser adminUser = adminUserMapper.selectByUsername(loginDTO.getUsername());
        if (adminUser == null) {
            return Result.fail("用户名或密码错误");
        }

        // 2. 检查账号状态
        if (adminUser.getStatus() != null && adminUser.getStatus() != 1) {
            return Result.fail("账号已被禁用");
        }

        // 3. BCrypt密码比对
        if (!BCrypt.checkpw(loginDTO.getPassword(), adminUser.getPassword())) {
            log.warn("Password mismatch for user: {}", loginDTO.getUsername());
            return Result.fail("用户名或密码错误");
        }

        // 4. 存入HttpSession（单账号登录）
        session.setAttribute("adminUser", adminUser);
        session.setMaxInactiveInterval(2 * 60 * 60); // 2小时过期

        // 5. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("nickname", adminUser.getNickname());
        result.put("role", adminUser.getRole() != null ? adminUser.getRole() : "admin");
        result.put("sessionId", session.getId());

        log.info("Admin login success: username={}, sessionId={}", loginDTO.getUsername(), session.getId());
        return Result.ok(result);
    }

    @GetMapping("/check")
    @Operation(summary = "登录状态校验")
    public Result<Map<String, Object>> checkLogin(HttpSession session) {
        AdminUser adminUser = (AdminUser) session.getAttribute("adminUser");
        if (adminUser == null) {
            return Result.fail(401, "未登录");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("nickname", adminUser.getNickname());
        result.put("role", adminUser.getRole());
        result.put("username", adminUser.getUsername());
        return Result.ok(result);
    }

    @PostMapping("/logout")
    @Operation(summary = "管理员登出")
    public Result<Void> logout(HttpSession session) {
        AdminUser adminUser = (AdminUser) session.getAttribute("adminUser");
        if (adminUser != null) {
            log.info("Admin logout: username={}", adminUser.getUsername());
        }
        session.invalidate();
        return Result.ok();
    }
}
