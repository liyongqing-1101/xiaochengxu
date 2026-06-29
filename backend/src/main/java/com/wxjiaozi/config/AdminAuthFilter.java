package com.wxjiaozi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxjiaozi.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthFilter implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AdminAuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Allow OPTIONS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        // Skip admin auth paths — the /api prefix is stripped by context-path
        if (path != null && path.contains("/admin/auth/")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtUtil.validateToken(token)) {
                writeUnauthorized(response, "未登录或登录已过期");
                return false;
            }

            String role = jwtUtil.getRoleFromToken(token);
            if (role == null || (!role.toUpperCase().contains("ADMIN") && !role.toUpperCase().contains("SUPER_ADMIN"))) {
                writeForbidden(response, "无权限访问");
                return false;
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            request.setAttribute("currentAdminId", userId);

            return true;
        } catch (Exception e) {
            log.error("Admin JWT authentication failed", e);
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String json = objectMapper.writeValueAsString(
                new ErrorBody(401, message, null)
        );
        response.getWriter().write(json);
    }

    private void writeForbidden(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        String json = objectMapper.writeValueAsString(
                new ErrorBody(403, message, null)
        );
        response.getWriter().write(json);
    }

    /**
     * Simple DTO for error response body matching the required JSON structure.
     */
    private static class ErrorBody {
        public int code;
        public String message;
        public Object data;

        public ErrorBody() {}

        public ErrorBody(int code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }
    }
}
