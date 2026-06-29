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
public class JwtAuthFilter implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

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
        // Skip auth paths — the /api prefix is stripped by context-path
        if (path != null && (path.contains("/auth/") || path.contains("/doc.html")
                || path.contains("/swagger-ui/") || path.contains("/v3/api-docs/"))) {
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

            Long userId = jwtUtil.getUserIdFromToken(token);
            String openid = jwtUtil.getOpenidFromToken(token);

            request.setAttribute("currentUserId", userId);
            request.setAttribute("currentOpenid", openid);

            return true;
        } catch (Exception e) {
            log.error("JWT authentication failed", e);
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String json = objectMapper.writeValueAsString(
                new UnauthorizedBody(401, message, null)
        );
        response.getWriter().write(json);
    }

    /**
     * Simple DTO for error response body matching the required JSON structure.
     */
    private static class UnauthorizedBody {
        public int code;
        public String message;
        public Object data;

        public UnauthorizedBody() {}

        public UnauthorizedBody(int code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }
    }
}
