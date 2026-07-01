package com.wxjiaozi.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * POI Excel读取工具类（简化版）
 * 支持6列模板：科目ID、题型、题干、选项、正确答案、解析
 */
@Slf4j
public class ExcelImportUtil {

    /** Excel列定义（简化版6列） */
    private static final int COL_SUBJECT_ID = 0;   // A: 科目ID
    private static final int COL_TYPE = 1;          // B: 题型
    private static final int COL_STEM = 2;          // C: 题干
    private static final int COL_OPTIONS = 3;       // D: 选项（用分号分隔）
    private static final int COL_ANSWER = 4;        // E: 正确答案
    private static final int COL_EXPLANATION = 5;   // F: 解析

    /**
     * 解析Excel（简化版，直接返回解析结果）
     * @param inputStream Excel文件输入流
     * @return 解析结果
     */
    public static ImportParseResult parseExcel(InputStream inputStream) {
        ImportParseResult result = new ImportParseResult();
        List<ImportQuestionRow> rows = new ArrayList<>();
        List<ImportErrorRow> errors = new ArrayList<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Excel文件中未找到工作表");
            }

            int lastRowNum = sheet.getLastRowNum();
            result.totalRows = Math.max(0, lastRowNum); // 不含表头

            // 逐行读取 (跳过表头 row 0)
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // 检查是否空行 (题干为空)
                String stem = getCellStringValue(row, COL_STEM);
                if (StrUtil.isBlank(stem)) continue;

                try {
                    ImportQuestionRow importRow = parseRow(row, i + 1);
                    rows.add(importRow);
                    result.successRows++;
                } catch (Exception e) {
                    result.failRows++;
                    errors.add(new ImportErrorRow(i + 1, stem, e.getMessage()));
                    log.warn("第{}行解析失败: {}", i + 1, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Excel解析失败", e);
            throw new RuntimeException("Excel解析失败: " + e.getMessage(), e);
        }

        result.rows = rows;
        result.errors = errors;
        return result;
    }

    /**
     * 解析单行数据（简化版6列）
     */
    private static ImportQuestionRow parseRow(Row row, int rowNum) {
        ImportQuestionRow dto = new ImportQuestionRow();
        dto.setRowNum(rowNum);

        // 题干
        String stem = getCellStringValue(row, COL_STEM);
        if (StrUtil.isBlank(stem)) {
            throw new IllegalArgumentException("题干不能为空");
        }
        dto.setStem(stem.trim());

        // 题型
        String typeStr = getCellStringValue(row, COL_TYPE);
        if (StrUtil.isBlank(typeStr)) {
            throw new IllegalArgumentException("题型不能为空");
        }
        int type;
        try {
            type = Integer.parseInt(typeStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("题型格式错误: " + typeStr + ", 应为1/2/3");
        }
        if (type < 1 || type > 3) {
            throw new IllegalArgumentException("题型值无效: " + type + ", 应为1(单选)/2(多选)/3(判断)");
        }
        dto.setType(type);

        // 科目ID
        String subIdStr = getCellStringValue(row, COL_SUBJECT_ID);
        if (StrUtil.isBlank(subIdStr)) {
            throw new IllegalArgumentException("科目ID不能为空");
        }
        try {
            dto.setSubjectId(Long.parseLong(subIdStr.trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("科目ID格式错误: " + subIdStr);
        }

        // 选项 (单选/多选必填，判断题留空)
        String options = getCellStringValue(row, COL_OPTIONS);
        if (type != 3 && StrUtil.isBlank(options)) {
            throw new IllegalArgumentException("单选/多选题的选项不能为空");
        }
        dto.setOptions(options != null ? options.trim() : "");

        // 正确答案
        String answer = getCellStringValue(row, COL_ANSWER);
        if (StrUtil.isBlank(answer)) {
            throw new IllegalArgumentException("正确答案不能为空");
        }
        answer = answer.trim().toUpperCase();
        if (type == 3) {
            // 判断题: 接受 T/正确/对/✓ 或 F/错误/错/✗
            if (answer.matches("[T是正确对✓ＹY]|TRUE|YES")) {
                answer = "T";
            } else if (answer.matches("[F否错误错✗ＮN]|FALSE|NO")) {
                answer = "F";
            } else {
                throw new IllegalArgumentException("判断题答案格式错误: " + answer + ", 应为T/F或正确/错误");
            }
        }
        dto.setAnswer(answer);

        // 解析
        dto.setExplanation(getCellStringValue(row, COL_EXPLANATION));

        return dto;
    }

    /**
     * 获取单元格字符串值
     */
    private static String getCellStringValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                // 处理数字类型, 避免科学计数法
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }

    // ========== 内部类 ==========

    @lombok.Data
    public static class ImportParseResult {
        private int totalRows;
        private int successRows;
        private int failRows;
        private List<ImportQuestionRow> rows;
        private List<ImportErrorRow> errors;
    }

    @lombok.Data
    public static class ImportQuestionRow {
        private int rowNum;
        private Long subjectId;
        private Integer type;
        private String stem;
        private String options;  // 选项用分号分隔
        private String answer;
        private String explanation;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ImportErrorRow {
        private int rowNum;
        private String stem;
        private String reason;
    }
}
