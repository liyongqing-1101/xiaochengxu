package com.wxjiaozi.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxjiaozi.dto.imports.ImportProgressDTO;
import com.wxjiaozi.entity.ExamQuestion;
import com.wxjiaozi.entity.ExamSubject;
import com.wxjiaozi.entity.ImportTask;
import com.wxjiaozi.mapper.ExamQuestionMapper;
import com.wxjiaozi.mapper.ExamSubjectMapper;
import com.wxjiaozi.mapper.ImportTaskMapper;
import com.wxjiaozi.service.ImportService;
import com.wxjiaozi.service.OssService;
import com.wxjiaozi.util.ExcelExportUtil;
import com.wxjiaozi.util.ExcelImportUtil;
import com.wxjiaozi.util.RedisKeyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 题库导入服务实现
 * 核心流程: 流式读取Excel → 分批校验 → 去重 → 批量入库 → Redis进度追踪 → 失败导出
 */
@Slf4j
@Service
public class ImportServiceImpl implements ImportService {

    private final ImportTaskMapper importTaskMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamSubjectMapper examSubjectMapper;
    private final OssService ossService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.import.batch-size:1000}")
    private int batchSize;

    @Value("${app.import.max-rows:100000}")
    private int maxRows;

    public ImportServiceImpl(ImportTaskMapper importTaskMapper,
                             ExamQuestionMapper examQuestionMapper,
                             ExamSubjectMapper examSubjectMapper,
                             OssService ossService,
                             StringRedisTemplate stringRedisTemplate,
                             ObjectMapper objectMapper) {
        this.importTaskMapper = importTaskMapper;
        this.examQuestionMapper = examQuestionMapper;
        this.examSubjectMapper = examSubjectMapper;
        this.ossService = ossService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ImportTask createImportTask(Long adminId, String fileName, Long fileSize, Long categoryId) {
        ImportTask task = new ImportTask();
        task.setAdminId(adminId);
        task.setFileName(fileName);
        task.setFileSize(fileSize);
        task.setCategoryId(categoryId);
        task.setTotalRows(0);
        task.setSuccessCount(0);
        task.setFailCount(0);
        task.setStatus("PENDING");
        importTaskMapper.insert(task);
        return task;
    }

    @Override
    @Async("importExecutor")
    public void executeImport(Long taskId, InputStream inputStream, Long categoryId) {
        // 更新状态为处理中
        updateTaskStatus(taskId, "PROCESSING", LocalDateTime.now());

        List<ExcelImportUtil.ImportErrorRow> allErrors = new ArrayList<>();
        int[] totalProcessed = {0};
        int[] totalFail = {0};
        int[] currentBatchSuccess = {0};

        try {
            // 流式解析Excel并分批入库, 每批回调时更新进度
            ExcelImportUtil.ImportParseResult parseResult = ExcelImportUtil.parseExcel(
                    inputStream, categoryId, batchSize, (batch) -> {
                        // 每批入库前先查询已存在的题目进行去重
                        List<ExamQuestion> newQuestions = filterDuplicates(batch);
                        int duplicateCount = batch.size() - newQuestions.size();
                        if (!newQuestions.isEmpty()) {
                            examQuestionMapper.insertBatch(newQuestions);
                        }
                        int batchSuccess = newQuestions.size();
                        currentBatchSuccess[0] += batchSuccess;
                        totalProcessed[0] += batch.size();

                        // 增量更新Redis进度
                        updateRedisProgress(taskId, totalProcessed[0], currentBatchSuccess[0],
                                totalFail[0], "PROCESSING");
                    });

            totalProcessed[0] = parseResult.getSuccessRows() + parseResult.getFailRows();
            totalFail[0] = parseResult.getFailRows();
            int totalSuccess = currentBatchSuccess[0];
            allErrors = parseResult.getErrors();

            // 更新导入任务
            ImportTask task = importTaskMapper.selectById(taskId);
            task.setTotalRows(totalProcessed[0]);
            task.setSuccessCount(totalSuccess);
            task.setFailCount(totalFail[0]);

            // 如果有失败行, 生成错误Excel并上传OSS
            if (!allErrors.isEmpty()) {
                try {
                    byte[] errorExcel = ExcelExportUtil.generateErrorExcel(allErrors);
                    String errorUrl = ossService.upload(
                            new ByteArrayInputStream(errorExcel),
                            "import_error_" + taskId + ".xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    );
                    task.setErrorFileUrl(errorUrl);
                    task.setErrorDetail(objectMapper.writeValueAsString(
                            allErrors.stream()
                                    .map(e -> String.format("行%d: %s", e.getRowNum(), e.getReason()))
                                    .toList()
                    ));
                } catch (Exception e) {
                    log.error("生成错误Excel失败", e);
                }
            }

            // 更新科目题目计数
            updateSubjectQuestionCount(categoryId);

            task.setStatus("COMPLETED");
            task.setCompletedAt(LocalDateTime.now());
            importTaskMapper.updateById(task);

            // 更新Redis进度为完成
            updateRedisProgress(taskId, totalProcessed[0], totalSuccess, totalFail[0], "COMPLETED");

            log.info("导入任务[{}]完成: 总数={}, 成功={}, 失败={}", taskId, totalProcessed[0], totalSuccess, totalFail[0]);

        } catch (Exception e) {
            log.error("导入任务[{}]失败", taskId, e);
            updateTaskStatus(taskId, "FAILED", null);
            ImportTask task = importTaskMapper.selectById(taskId);
            task.setErrorDetail("导入异常: " + e.getMessage());
            task.setCompletedAt(LocalDateTime.now());
            importTaskMapper.updateById(task);

            updateRedisProgress(taskId, totalProcessed[0], currentBatchSuccess[0], totalFail[0], "FAILED");
        }
    }

    @Override
    public ImportProgressDTO getProgress(Long taskId) {
        // 先查Redis
        String redisKey = RedisKeyUtil.importProgressKey(taskId);
        String redisData = stringRedisTemplate.opsForValue().get(redisKey);
        if (StrUtil.isNotBlank(redisData)) {
            try {
                return objectMapper.readValue(redisData, ImportProgressDTO.class);
            } catch (JsonProcessingException e) {
                log.warn("解析Redis进度失败", e);
            }
        }

        // 回退查DB
        ImportTask task = importTaskMapper.selectById(taskId);
        if (task == null) return null;

        ImportProgressDTO dto = new ImportProgressDTO();
        dto.setTaskId(taskId);
        dto.setTotalRows(task.getTotalRows());
        dto.setCurrentRow(task.getTotalRows()); // DB中没有current, 用total替代
        dto.setSuccessCount(task.getSuccessCount());
        dto.setFailCount(task.getFailCount());
        dto.setPercent(task.getTotalRows() != null && task.getTotalRows() > 0
                ? task.getSuccessCount() * 100 / task.getTotalRows() : 0);
        dto.setStatus(task.getStatus());
        return dto;
    }

    @Override
    public void updateProgress(Long taskId, int currentRow, int successCount, int failCount, String status) {
        ImportProgressDTO dto = new ImportProgressDTO();
        dto.setTaskId(taskId);
        dto.setCurrentRow(currentRow);
        dto.setSuccessCount(successCount);
        dto.setFailCount(failCount);
        dto.setPercent(currentRow > 0 ? (successCount + failCount) * 100 / currentRow : 0);
        dto.setStatus(status);

        try {
            String json = objectMapper.writeValueAsString(dto);
            stringRedisTemplate.opsForValue().set(
                    RedisKeyUtil.importProgressKey(taskId),
                    json,
                    RedisKeyUtil.IMPORT_PROGRESS_TTL,
                    TimeUnit.SECONDS
            );
        } catch (JsonProcessingException e) {
            log.error("序列化进度失败", e);
        }
    }

    @Override
    public List<ImportTask> getImportTasks(int page, int pageSize) {
        Page<ImportTask> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<ImportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ImportTask::getCreateTime);
        IPage<ImportTask> result = importTaskMapper.selectPage(p, wrapper);
        return result.getRecords();
    }

    @Override
    public byte[] downloadErrorFile(Long taskId) {
        ImportTask task = importTaskMapper.selectById(taskId);
        if (task == null || StrUtil.isBlank(task.getErrorFileUrl())) {
            throw new RuntimeException("错误文件不存在");
        }
        // 从URL提取objectKey
        String url = task.getErrorFileUrl();
        String objectKey = url.substring(url.lastIndexOf('/') + 1);
        // 实际从完整URL提取完整路径
        if (url.contains(".com/")) {
            objectKey = url.substring(url.indexOf(".com/") + 5);
        }
        return ossService.download(objectKey);
    }

    // ========== 私有方法 ==========

    private void updateTaskStatus(Long taskId, String status, LocalDateTime startedAt) {
        ImportTask task = importTaskMapper.selectById(taskId);
        task.setStatus(status);
        if (startedAt != null) {
            task.setStartedAt(startedAt);
        }
        importTaskMapper.updateById(task);
    }

    private void updateRedisProgress(Long taskId, int total, int success, int fail, String status) {
        ImportProgressDTO dto = new ImportProgressDTO();
        dto.setTaskId(taskId);
        dto.setTotalRows(total);
        dto.setCurrentRow(total);
        dto.setSuccessCount(success);
        dto.setFailCount(fail);
        dto.setPercent(total > 0 ? success * 100 / total : 100);
        dto.setStatus(status);

        try {
            stringRedisTemplate.opsForValue().set(
                    RedisKeyUtil.importProgressKey(taskId),
                    objectMapper.writeValueAsString(dto),
                    RedisKeyUtil.IMPORT_PROGRESS_TTL,
                    TimeUnit.SECONDS
            );
        } catch (Exception e) {
            log.error("更新Redis进度失败", e);
        }
    }

    /**
     * 去重: 使用题干MD5与已有题目比对, 过滤重复
     * 首次调用时从DB加载已有MD5集合到内存缓存
     */
    private Set<String> existingMd5Cache = null;

    private List<ExamQuestion> filterDuplicates(List<ExamQuestion> batch) {
        if (existingMd5Cache == null) {
            // 延迟加载: 首次调用时查询所有已有题目的题干MD5
            List<ExamQuestion> allExisting = examQuestionMapper.selectList(
                    new LambdaQueryWrapper<ExamQuestion>()
                            .select(ExamQuestion::getStem)
            );
            existingMd5Cache = allExisting.stream()
                    .map(q -> DigestUtil.md5Hex(q.getStem()))
                    .collect(Collectors.toSet());
            log.info("加载已有题目MD5缓存, 共{}条", existingMd5Cache.size());
        }

        List<ExamQuestion> newQuestions = new ArrayList<>();
        for (ExamQuestion q : batch) {
            String md5 = DigestUtil.md5Hex(q.getStem());
            if (!existingMd5Cache.contains(md5)) {
                existingMd5Cache.add(md5); // 加入缓存避免同批次重复
                newQuestions.add(q);
            }
        }
        return newQuestions;
    }

    /**
     * 更新科目题目总数
     */
    private void updateSubjectQuestionCount(Long categoryId) {
        List<ExamSubject> subjects = examSubjectMapper.selectList(
                new LambdaQueryWrapper<ExamSubject>()
                        .eq(ExamSubject::getCategoryId, categoryId)
        );
        for (ExamSubject subject : subjects) {
            Long count = examQuestionMapper.selectCount(
                    new LambdaQueryWrapper<ExamQuestion>()
                            .eq(ExamQuestion::getSubjectId, subject.getId())
                            .eq(ExamQuestion::getStatus, 1)
            );
            examSubjectMapper.update(null,
                    new LambdaUpdateWrapper<ExamSubject>()
                            .eq(ExamSubject::getId, subject.getId())
                            .set(ExamSubject::getTotalQuestions, count.intValue())
            );
        }
        log.info("已更新分类[{}]下{}个科目的题目计数", categoryId, subjects.size());
    }
}
