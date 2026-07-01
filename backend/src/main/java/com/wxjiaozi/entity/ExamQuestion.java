package com.wxjiaozi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目表实体类（MyBatis原生版）
 * 管理后台简化版：仅保留必要字段
 * 小程序端保持optionList为List<String>，兼容原有逻辑
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestion {

    /** 主键ID */
    private Long id;

    /** 科目ID */
    private Long subjectId;

    /** 题型：1=单选题，2=多选题，3=判断题 */
    private Integer type;

    /** 题干内容 */
    private String stem;

    /**
     * 题目选项数组（JSON格式）
     * 存储格式：["选项A内容","选项B内容","选项C内容","选项D内容"]
     * 判断题此字段为 null
     */
    private List<String> optionList;

    /**
     * 正确答案
     * 单选：单个字母，如 A、D
     * 多选：多个字母英文逗号分隔，如 A,C,E,G
     * 判断：固定存储 "T" 或 "F"
     */
    private String answer;

    /** 答案解析 */
    private String explanation;

    /** 状态：0=禁用，1=正常 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
