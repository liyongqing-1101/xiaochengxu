package com.wxjiaozi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目选项DTO
 * 用于 JSON 字段 option_list 的映射
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionDTO {

    /**
     * 选项标识，如 A、B、C、D、E、F、G 等
     */
    private String key;

    /**
     * 选项内容
     */
    private String value;
}
