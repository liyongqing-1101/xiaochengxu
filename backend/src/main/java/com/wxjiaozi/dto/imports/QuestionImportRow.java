package com.wxjiaozi.dto.imports;

import lombok.Data;

/**
 * Excel导入行数据 DTO
 */
@Data
public class QuestionImportRow {
    /** 题干 (HTML) */
    private String stem;
    /** 题型: 1单选 2多选 3判断 */
    private Integer type;
    /** 选项A */
    private String optionA;
    /** 选项B */
    private String optionB;
    /** 选项C */
    private String optionC;
    /** 选项D */
    private String optionD;
    /** 正确答案: 单选"A" 多选"A,C" 判断"T"/"F" */
    private String answer;
    /** 解析 */
    private String explanation;
    /** 难度: 1简单 2中等 3困难, 默认2 */
    private Integer difficulty;
    /** 科目ID */
    private Long subjectId;
    /** 知识点标签名, 逗号分隔 */
    private String tagNames;
    /** Excel行号, 用于错误报告 */
    private int rowNum;
}
