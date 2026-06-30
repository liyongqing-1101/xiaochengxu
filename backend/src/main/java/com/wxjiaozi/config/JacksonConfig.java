package com.wxjiaozi.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Jackson 全局 UTF-8 序列化配置
 * 【作用】防止JSON中文转码异常，确保请求响应中文不乱码
 *
 * 配置要点：
 * 1. 默认编码 UTF-8
 * 2. 日期格式化：yyyy-MM-dd HH:mm:ss
 * 3. 时区：Asia/Shanghai
 * 4. 空对象不抛异常
 * 5. 未知属性不抛异常
 * 6. Java 8 时间类型（LocalDateTime）支持
 *
 * @author wxjiaozi
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // ============================================================
        // 1. 字符编码设置（核心：强制UTF-8，防止中文乱码）
        // ============================================================
        // 确保所有 JSON 序列化/反序列化都使用 UTF-8 编码
        // 配合 spring.http.encoding.charset=UTF-8 全局生效

        // ============================================================
        // 2. 日期格式化配置
        // ============================================================
        // 统一日期格式，避免 Date/LocalDateTime 序列化异常
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 设置为中国时区，确保时间字段正确
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        // ============================================================
        // 3. 容错配置（防止异常中断请求）
        // ============================================================
        // JSON 中包含未知属性时不抛出异常（兼容前端多余字段）
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 空对象序列化时不抛异常（如 new Object()）
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 日期不以时间戳格式输出
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // ============================================================
        // 4. Java 8 时间类型支持（LocalDateTime/LocalDate等）
        // ============================================================
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        objectMapper.registerModule(javaTimeModule);

        return objectMapper;
    }
}
