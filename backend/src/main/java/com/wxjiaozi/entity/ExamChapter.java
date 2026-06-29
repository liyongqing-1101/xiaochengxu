package com.wxjiaozi.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.wxjiaozi.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("exam_chapter")
public class ExamChapter extends BaseEntity {

    private Long subjectId;
    private String name;
    private Integer questionCount;
    private Integer sortOrder;

    @TableField(exist = false)
    private List<ExamTag> tags;
}
