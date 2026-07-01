package com.wxjiaozi.config;

import com.wxjiaozi.security.CurrentUserResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC配置
 * CORS已由CookieConfig中的CorsFilter统一处理
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private CurrentUserResolver currentUserResolver;

    // 管理后台Session拦截器
    @Autowired
    private AdminSessionInterceptor adminSessionInterceptor;

    // 小程序JWT拦截器
    @Autowired
    private WxJwtAuthFilter wxJwtAuthFilter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // ========================================
        // 管理后台拦截器：/admin/**
        // ========================================
        registry.addInterceptor(adminSessionInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/auth/login"     // 登录接口排除拦截
                );

        // ========================================
        // 小程序拦截器：/wx/**
        // 注意：Spring Boot 匹配路径不包含 context-path（/api），
        // 但实际请求路径会带，所以两种写法都加确保生效
        // ========================================
        registry.addInterceptor(wxJwtAuthFilter)
                .addPathPatterns("/wx/**")
                .excludePathPatterns(
                        "/wx/auth/login",         // 小程序登录排除拦截
                        "/wx/auth/register",      // 小程序注册排除拦截
                        "/wx/exam/categories",    // 分类列表（首页未登录可访问）
                        "/wx/exam/subjects",      // 科目列表（首页未登录可访问）
                        "/wx/question/subject/*/stats",  // 科目统计（首页未登录可访问）
                        "/wx/question/daily"      // 每日一题（未登录可访问）
                );
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserResolver);
    }
}
