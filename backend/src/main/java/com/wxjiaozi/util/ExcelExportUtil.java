package com.wxjiaozi.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * POI Excel导出工具类
 * - 生成导入模板
 * - 导出失败数据
 */
@Slf4j
public class ExcelExportUtil {

    /** 模板表头（简化版：仅保留数据库实际存在的字段） */
    private static final String[] TEMPLATE_HEADERS = {
            "科目ID", "题型", "题干", "选项", "正确答案", "解析"
    };

    /** 模板示例数据 */
    private static final String[] EXAMPLE_ROW = {
            "1",
            "1",
            "以下关于教育学说法正确的是()",
            "选项A内容;选项B内容;选项C内容;选项D内容",
            "A",
            "教育学是研究教育现象和教育问题,揭示教育规律的科学"
    };

    /**
     * 生成简化版导入模板Excel
     * @return Excel字节数组
     */
    public static byte[] generateTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("题库导入模板");

            // 创建标题样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle exampleStyle = createExampleStyle(workbook);

            // 表头行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < TEMPLATE_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(TEMPLATE_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 示例行
            Row exampleRow = sheet.createRow(1);
            for (int i = 0; i < EXAMPLE_ROW.length; i++) {
                Cell cell = exampleRow.createCell(i);
                cell.setCellValue(EXAMPLE_ROW[i]);
                cell.setCellStyle(exampleStyle);
            }

            // 题型列添加数据验证 (下拉: 1,2,3)
            addTypeValidation(sheet);

            // 设置列宽
            sheet.setColumnWidth(0, 10 * 256);  // 科目ID
            sheet.setColumnWidth(1, 8 * 256);   // 题型
            sheet.setColumnWidth(2, 40 * 256);  // 题干
            sheet.setColumnWidth(3, 50 * 256);  // 选项
            sheet.setColumnWidth(4, 12 * 256);  // 正确答案
            sheet.setColumnWidth(5, 40 * 256);  // 解析

            // 冻结表头
            sheet.createFreezePane(0, 1);

            // 添加说明sheet
            Sheet instructionSheet = workbook.createSheet("填写说明");
            Row instrRow = instructionSheet.createRow(0);
            instrRow.createCell(0).setCellValue("字段");
            instrRow.createCell(1).setCellValue("说明");
            instrRow.createCell(2).setCellValue("是否必填");
            instrRow.createCell(3).setCellValue("示例");

            addInstruction(instructionSheet, 1, "科目ID", "1=高等教育学,2=高等教育法规,3=教师伦理学,4=大学心理学", "是", "1");
            addInstruction(instructionSheet, 2, "题型", "1=单选 2=多选 3=判断", "是", "1");
            addInstruction(instructionSheet, 3, "题干", "题目内容", "是", "以下关于教育学说法正确的是()");
            addInstruction(instructionSheet, 4, "选项", "多个选项用分号(;)分隔，判断题留空", "单选/多选必填", "选项A;选项B;选项C;选项D");
            addInstruction(instructionSheet, 5, "正确答案", "单选:A/B/C/D 多选:A,C 判断:T/F", "是", "A");
            addInstruction(instructionSheet, 6, "解析", "题目解析", "否", "教育学是研究教育现象和教育问题...");

            instructionSheet.setColumnWidth(0, 15 * 256);
            instructionSheet.setColumnWidth(1, 45 * 256);
            instructionSheet.setColumnWidth(2, 15 * 256);
            instructionSheet.setColumnWidth(3, 40 * 256);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("生成模板失败", e);
            throw new RuntimeException("生成模板失败", e);
        }
    }

    /**
     * 生成导入失败数据Excel
     * @param errors 失败行列表
     * @return Excel字节数组
     */
    public static byte[] generateErrorExcel(List<ExcelImportUtil.ImportErrorRow> errors) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("导入失败明细");

            CellStyle headerStyle = createHeaderStyle(workbook);

            // 表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Excel行号");
            headerRow.createCell(1).setCellValue("题干(截取)");
            headerRow.createCell(2).setCellValue("失败原因");
            for (int i = 0; i < 3; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }

            // 错误行
            for (int i = 0; i < errors.size(); i++) {
                Row row = sheet.createRow(i + 1);
                ExcelImportUtil.ImportErrorRow err = errors.get(i);
                row.createCell(0).setCellValue(err.getRowNum());
                String stem = err.getStem();
                if (stem != null && stem.length() > 100) {
                    stem = stem.substring(0, 100) + "...";
                }
                row.createCell(1).setCellValue(stem != null ? stem : "");
                row.createCell(2).setCellValue(err.getReason());
            }

            sheet.setColumnWidth(0, 12 * 256);
            sheet.setColumnWidth(1, 50 * 256);
            sheet.setColumnWidth(2, 40 * 256);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("生成错误Excel失败", e);
            throw new RuntimeException("生成错误Excel失败", e);
        }
    }

    // ========== 私有辅助方法 ==========

    private static void addInstruction(Sheet sheet, int rowNum, String field, String desc, String required, String example) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(field);
        row.createCell(1).setCellValue(desc);
        row.createCell(2).setCellValue(required);
        row.createCell(3).setCellValue(example);
    }

    private static CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private static CellStyle createExampleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static void addTypeValidation(Sheet sheet) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createExplicitListConstraint(new String[]{"1", "2", "3"});
        CellRangeAddressList addressList = new CellRangeAddressList(1, 100000, 1, 1);
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setShowErrorBox(true);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("题型错误", "请输入1(单选)、2(多选)或3(判断)");
        sheet.addValidationData(validation);
    }
}
