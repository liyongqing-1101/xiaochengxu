package com.wxjiaozi.dto.mini;

import lombok.Data;

/**
 * 科目题目统计（按题型）
 */
@Data
public class SubjectStatsDTO {

    /** 单选题数量 */
    private int singleCount;

    /** 多选题数量 */
    private int multiCount;

    /** 判断题数量 */
    private int trueFalseCount;

    /** 总题量 */
    private int totalCount;
}
