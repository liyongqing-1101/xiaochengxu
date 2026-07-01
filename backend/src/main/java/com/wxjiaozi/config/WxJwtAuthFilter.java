package com.wxjiaozi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxjiaozi.common.Result;
import com.wxjiaozi.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

@Slf4j
@Component
public class WxJwtAuthFilter implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String requestURI = request.getRequestURI();
        log.debug("WxJwtAuthFilter intercepting: {}", requestURI);

        // 白名单：公开接口直接放行
        if (requestURI.contains("/wx/auth/login")
                || requestURI.contains("/wx/exam/categories")
                || requestURI.contains("/wx/exam/subjects")
                || requestURI.matches(".*/wx/question/subject/\\d+/stats.*")
                || requestURI.contains("/wx/question/daily")) {
            log.debug("Whitelisted path, passing through: {}", requestURI);
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未提供认证token");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            writeUnauthorized(response, "token无效或已过期");
            return false;
        }

        // 将用户信息存入request供CurrentUserResolver使用
        Long userId = jwtUtil.getUserIdFromToken(token);
        String openid = jwtUtil.getOpenidFromToken(token);
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentOpenid", openid);

        log.info("Wx request: userId={}, openid={}, path={}", userId, openid, request.getRequestURI());
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
