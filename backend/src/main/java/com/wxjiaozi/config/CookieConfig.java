package com.wxjiaozi.config;

import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Cookie跨域配置
 * 确保Session Cookie能被前端正确携带
 */
@Configuration
public class CookieConfig {

    /**
     * 配置Cookie SameSite策略
     * 前端开发环境使用Lax模式，生产环境使用None+Secure
     */
    @Bean
    public CookieSameSiteSupplier cookieSameSiteSupplier() {
        return CookieSameSiteSupplier.ofLax();
    }

    /**
     * 全局CORS配置，覆盖WebMvcConfigurer
     * 确保withCredentials=true时能正常携带Cookie
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许所有来源（生产环境应限制具体域名）
        config.addAllowedOriginPattern("*");

        // 允许携带Cookie (关键!)
        config.setAllowCredentials(true);

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许所有方法
        config.addAllowedMethod("*");

        // 暴露响应头（供前端读取）
        config.addExposedHeader(HttpHeaders.SET_COOKIE);
        config.addExposedHeader(HttpHeaders.AUTHORIZATION);

        // 预检请求缓存时间
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
