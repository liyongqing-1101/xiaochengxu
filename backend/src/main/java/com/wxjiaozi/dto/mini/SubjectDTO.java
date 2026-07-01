package com.wxjiaozi.dto.mini;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 首页科目列表DTO
 * <p>
 * 包含科目基本信息 + 用户已做题数 / 科目总题数，
 * 供小程序首页渲染进度条。
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDTO {

    /** 科目ID */
    private Long id;

    /** 所属分类ID */
    private Long categoryId;

    /** 科目名称 */
    private String name;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sortOrder;

    /** 状态：0禁用 1启用 */
    private Integer status;

    /** 科目总题数（从exam_subject.total_questions读取） */
    private Integer totalQuestions;

    /** 用户该科目已做题数（去重） */
    private Long doneCount;
}
