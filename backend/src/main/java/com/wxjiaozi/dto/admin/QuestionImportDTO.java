package com.wxjiaozi.dto.admin;

import lombok.Data;

import java.util.List;

/**
 * 题目导入DTO
 * 仅包含实际表中存在的字段
 */
@Data
public class QuestionImportDTO {

    /**
     * 科目ID（必填）
     */
    private Long subjectId;

    /**
     * 题型（1=单选，2=多选，3=判断）
     */
    private Integer type;

    /**
     * 题干
     */
    private String stem;

    /**
     * 选项（单选/多选：分号或换行分隔；判断：留空）
     */
    private String options;

    /**
     * 选项列表（解析后）
     */
    private List<String> optionList;

    /**
     * 正确答案（单选/多选：字母；判断：T/F）
     */
    private String answer;

    /**
     * 解析
     */
    private String explanation;

    /**
     * 行号（用于错误提示）
     */
    private Integer rowNum;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 校验数据合法性
     */
    public boolean validate() {
        StringBuilder errors = new StringBuilder();

        if (subjectId == null) {
            errors.append("科目ID不能为空; ");
        }
        if (type == null) {
            errors.append("题型不能为空; ");
        } else if (type < 1 || type > 3) {
            errors.append("题型只能是1(单选),2(多选),3(判断); ");
        }
        if (stem == null || stem.trim().isEmpty()) {
            errors.append("题干不能为空; ");
        }
        if (answer == null || answer.trim().isEmpty()) {
            errors.append("正确答案不能为空; ");
        }

        // 解析选项
        if (type != null && type != 3) { // 判断题不需要选项
            if (options != null && !options.trim().isEmpty()) {
                // 支持分号、换行、逗号分隔
                String[] parts = options.split("[;\\n,，]");
                optionList = java.util.Arrays.stream(parts)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(java.util.stream.Collectors.toList());
            }
            if (optionList == null || optionList.isEmpty()) {
                errors.append("单选/多选题必须有选项; ");
            }
        } else {
            // 判断题optionList设为null
            optionList = null;
        }

        if (errors.length() > 0) {
            errorMsg = errors.toString();
            return false;
        }
        return true;
    }
}
