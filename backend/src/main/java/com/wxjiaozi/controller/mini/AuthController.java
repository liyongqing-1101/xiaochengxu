package com.wxjiaozi.controller.mini;

import com.wxjiaozi.common.Result;
import com.wxjiaozi.dto.mini.LoginDTO;
import com.wxjiaozi.dto.mini.LoginResultDTO;
import com.wxjiaozi.security.JwtUtil;
import com.wxjiaozi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "小程序-认证")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "微信登录")
    public Result<LoginResultDTO> login(@RequestBody @Valid LoginDTO loginDTO) {
        LoginResultDTO result = userService.loginByWechat(loginDTO.getCode());
        return Result.ok(result);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token")
    public Result<Map<String, String>> refresh(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!jwtUtil.validateToken(token)) {
            return Result.fail(401, "Token无效或已过期");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        String openid = jwtUtil.getOpenidFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        String newToken = jwtUtil.generateToken(userId, openid, role);

        Map<String, String> data = new HashMap<>();
        data.put("token", newToken);
        return Result.ok(data);
    }
}
