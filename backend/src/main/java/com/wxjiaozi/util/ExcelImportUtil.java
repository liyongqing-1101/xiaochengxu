package com.wxjiaozi.util;

import cn.hutool.core.util.StrUtil;
import com.wxjiaozi.dto.imports.QuestionImportRow;
import com.wxjiaozi.entity.ExamQuestion;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * POI Excel流式读取工具类
 * 使用XSSFWorkbook分批读取, 每batchSize行回调一次消费者
 * 最大支持10万行, 通过分批处理避免OOM
 */
@Slf4j
public class ExcelImportUtil {

    /** Excel列定义 */
    private static final int COL_STEM = 0;        // A: 题干
    private static final int COL_TYPE = 1;        // B: 题型
    private static final int COL_OPTION_A = 2;    // C: 选项A
    private static final int COL_OPTION_B = 3;    // D: 选项B
    private static final int COL_OPTION_C = 4;    // E: 选项C
    private static final int COL_OPTION_D = 5;    // F: 选项D
    private static final int COL_ANSWER = 6;      // G: 正确答案
    private static final int COL_EXPLANATION = 7; // H: 解析
    private static final int COL_DIFFICULTY = 8;  // I: 难度
    private static final int COL_SUBJECT_ID = 9;  // J: 科目ID
    private static final int COL_TAG_NAMES = 10;  // K: 知识点标签

    /**
     * 解析Excel并分批回调
     * @param inputStream Excel文件输入流
     * @param categoryId  考试大类ID
     * @param batchSize   每批大小
     * @param consumer    批次消费者 (接收解析后的ExamQuestion列表)
     * @return 解析统计结果
     */
    public static ImportParseResult parseExcel(
            InputStream inputStream,
            Long categoryId,
            int batchSize,
            Consumer<List<ExamQuestion>> consumer) {

        ImportParseResult result = new ImportParseResult();
        List<ExamQuestion> batch = new ArrayList<>(batchSize);
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
                    QuestionImportRow importRow = parseRow(row, i + 1);
                    ExamQuestion question = convertToEntity(importRow, categoryId);
                    batch.add(question);
                    result.successRows++;

                    // 达到批次大小, 回调
                    if (batch.size() >= batchSize) {
                        consumer.accept(new ArrayList<>(batch));
                        batch.clear();
                    }
                } catch (Exception e) {
                    result.failRows++;
                    errors.add(new ImportErrorRow(i + 1, stem, e.getMessage()));
                    log.warn("第{}行解析失败: {}", i + 1, e.getMessage());
                }
            }

            // 处理最后一批
            if (!batch.isEmpty()) {
                consumer.accept(new ArrayList<>(batch));
            }

        } catch (Exception e) {
            log.error("Excel解析失败", e);
            throw new RuntimeException("Excel解析失败: " + e.getMessage(), e);
        }

        result.errors = errors;
        return result;
    }

    /**
     * 解析单行数据
     */
    private static QuestionImportRow parseRow(Row row, int rowNum) {
        QuestionImportRow dto = new QuestionImportRow();
        dto.setRowNum(rowNum);

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

        // 选项 (判断题跳过选项校验)
        if (type != 3) {
            dto.setOptionA(getCellStringValue(row, COL_OPTION_A));
            dto.setOptionB(getCellStringValue(row, COL_OPTION_B));
            dto.setOptionC(getCellStringValue(row, COL_OPTION_C));
            dto.setOptionD(getCellStringValue(row, COL_OPTION_D));

            if (StrUtil.isBlank(dto.getOptionA()) || StrUtil.isBlank(dto.getOptionB())) {
                throw new IllegalArgumentException("单选/多选题的选项A和选项B不能为空");
            }
        }

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

        // 难度
        String diffStr = getCellStringValue(row, COL_DIFFICULTY);
        if (StrUtil.isNotBlank(diffStr)) {
            try {
                int diff = Integer.parseInt(diffStr.trim());
                if (diff >= 1 && diff <= 3) {
                    dto.setDifficulty(diff);
                } else {
                    dto.setDifficulty(2); // 默认中等
                }
            } catch (NumberFormatException e) {
                dto.setDifficulty(2);
            }
        } else {
            dto.setDifficulty(2);
        }

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

        // 知识点标签
        dto.setTagNames(getCellStringValue(row, COL_TAG_NAMES));

        return dto;
    }

    /**
     * 将ImportRow转换为ExamQuestion实体
     */
    private static ExamQuestion convertToEntity(QuestionImportRow row, Long categoryId) {
        ExamQuestion q = new ExamQuestion();
        q.setSubjectId(row.getSubjectId());
        q.setType(row.getType());
        q.setStem(row.getStem());

        // 转换答案为新存储格式
        q.setAnswer(convertAnswerFormat(row.getAnswer(), row.getType()));

        q.setExplanation(row.getExplanation());
        q.setStatus(1); // 默认上架

        // TODO: optionList 从旧选项字段转换
        // q.setOptionList(convertToOptionList(row));

        return q;
    }

    /**
     * 将答案字符串转换为新存储格式
     * 单选"A" -> "A"
     * 多选"A,C" -> "A,C"
     * 判断"T"/"F" -> "true"/"false"
     */
    public static String convertAnswerFormat(String answer, int type) {
        if (answer == null) return null;
        String trimmed = answer.trim();
        // 判断题
        if (type == 3) {
            if ("T".equalsIgnoreCase(trimmed) || "TRUE".equalsIgnoreCase(trimmed)) {
                return "true";
            }
            if ("F".equalsIgnoreCase(trimmed) || "FALSE".equalsIgnoreCase(trimmed)) {
                return "false";
            }
            return "true"; // 默认
        }
        // 多选/单选: 字母大写，保持逗号分隔格式
        return trimmed.toUpperCase();
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
        private List<ImportErrorRow> errors;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ImportErrorRow {
        private int rowNum;
        private String stem;
        private String reason;
    }
}
