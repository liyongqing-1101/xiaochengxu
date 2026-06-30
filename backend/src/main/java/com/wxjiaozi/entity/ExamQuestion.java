package com.wxjiaozi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.wxjiaozi.dto.QuestionOptionDTO;
import com.wxjiaozi.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 题目表（v2.0 重构版）
 *
 * 存储规则：
 * 1. type 字段：1=单选题，2=多选题，3=判断题
 * 2. option_list 字段：仅单选、多选题填充JSON数组，判断题此字段为null
 * 3. answer 字段：
 *    - 单选：单个字母，如 A、D
 *    - 多选：多个字母英文逗号分隔，如 A,C,E,G
 *    - 判断：固定存储 "true" 或 "false"
 * 4. status 字段：默认1，1=正常，0=禁用，抽卷、统计时仅过滤status=0的数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "exam_question", autoResultMap = true)
public class ExamQuestion extends BaseEntity {

    /**
     * 科目ID
     * 1=高等教育学 2=高等教育法规和政策 3=教师伦理学 4=大学心理学
     */
    private Long subjectId;

    /**
     * 题型：1=单选题，2=多选题，3=判断题
     */
    private Integer type;

    /**
     * 题干内容（支持HTML格式）
     */
    private String stem;

    /**
     * 题目选项数组
     * 存储格式：[{"key":"A","value":"选项内容"},{"key":"E","value":"选项内容"}]
     * 兼容A~G多选项
     * 判断题此字段为null
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<QuestionOptionDTO> optionList;

    /**
     * 正确答案
     * 单选：单个字母，如 A、D
     * 多选：多个字母英文逗号分隔，如 A,C,E,G
     * 判断：固定存储 "true" 或 "false"
     */
    private String answer;

    /**
     * 答案解析（支持HTML格式）
     */
    private String explanation;

    /**
     * 状态：0=禁用，1=正常
     * 抽卷、统计题量时全部过滤 status=0 的数据
     */
    private Integer status;
}
