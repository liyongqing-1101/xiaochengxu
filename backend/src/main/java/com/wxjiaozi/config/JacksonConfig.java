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
// 暂时禁用 JacksonConfig，使用默认配置
// @Configuration
public class JacksonConfig {

}
