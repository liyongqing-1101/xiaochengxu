package com.wxjiaozi.controller.admin;

import com.wxjiaozi.common.Result;
import com.wxjiaozi.dto.admin.AdminLoginDTO;
import com.wxjiaozi.dto.admin.AdminLoginResultDTO;
import com.wxjiaozi.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/auth")
@Tag(name = "管理后台-认证")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<AdminLoginResultDTO> login(@RequestBody @Valid AdminLoginDTO loginDTO) {
        AdminLoginResultDTO result = adminService.login(loginDTO.getUsername(), loginDTO.getPassword());
        return Result.ok(result);
    }
}
