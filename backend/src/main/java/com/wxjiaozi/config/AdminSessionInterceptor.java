package com.wxjiaozi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxjiaozi.common.Result;
import com.wxjiaozi.entity.AdminUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

@Slf4j
@Component
public class AdminSessionInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许OPTIONS请求通过
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            writeUnauthorized(response, "未登录或会话已过期");
            return false;
        }

        AdminUser adminUser = (AdminUser) session.getAttribute("adminUser");
        if (adminUser == null) {
            writeUnauthorized(response, "未登录或会话已过期");
            return false;
        }

        // 将adminId存入request供CurrentUserResolver使用
        request.setAttribute("currentAdminId", adminUser.getId());
        log.info("Admin request: userId={}, path={}", adminUser.getId(), request.getRequestURI());
        return true;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(Result.fail(401, message)));
        writer.flush();
        writer.close();
    }
}
