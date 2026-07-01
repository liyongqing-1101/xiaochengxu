package com.wxjiaozi.service;

import com.wxjiaozi.dto.mini.SubjectDTO;
import com.wxjiaozi.dto.mini.SubmitAnswerDTO;
import com.wxjiaozi.dto.mini.SubmitResultDTO;

import java.util.List;

/**
 * 用户做题记录服务
 */
public interface RecordService {

    /**
     * 提交答题结果
     * <p>
     * 逻辑：
     * 1. 判断题目答案是否正确
     * 2. 写入exam_record表（唯一索引去重，重复刷题不新增行）
     * 3. 同步Redis INCR（首次做题才计数，重复刷题不计数）
     * </p>
     *
     * @param userId 用户ID
     * @param params 提交参数（sessionId, questionId, selectedOptions, duration）
     * @return 答题结果
     */
    SubmitResultDTO submitAnswer(Long userId, SubmitAnswerDTO params);

    /**
     * 获取首页科目列表（含用户已做题数）
     * <p>
     * 优先读Redis缓存user:subject:done:{userId}:{subjectId}，
     * 缓存不存在则查MySQL统计并回填Redis。
     * </p>
     *
     * @param userId     用户ID（可为null，未登录返回doneCount=0）
     * @param categoryId 分类ID
     * @return 科目列表
     */
    List<SubjectDTO> getSubjectList(Long userId, Long categoryId);

    /**
     * 删除用户做题记录
     * <p>
     * 同步Redis DECR扣减已做题计数。
     * </p>
     *
     * @param userId     用户ID
     * @param questionId 题目ID
     */
    void deleteRecord(Long userId, Long questionId);
}
