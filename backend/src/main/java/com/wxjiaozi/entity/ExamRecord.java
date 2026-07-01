package com.wxjiaozi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户做题明细表（去重版）
 * <p>
 * 同一个用户同一道题仅保留一条记录（uk_user_question唯一约束），
 * 重复刷题不新增行，只更新is_correct字段。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamRecord {

    /** 主键ID */
    private Long id;

    /** 小程序用户ID */
    private Long userId;

    /** 科目ID */
    private Long subjectId;

    /** 作答题目ID */
    private Long questionId;

    /** 是否正确：0=错误，1=正确 */
    private Integer isCorrect;

    /** 答题时间 */
    private LocalDateTime createTime;
}
