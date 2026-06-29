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
@TableName("exam_category")
public class ExamCategory extends BaseEntity {

    private String name;
    private String icon;
    private String description;
    private Integer sortOrder;
    private Integer status;

    @TableField(exist = false)
    private List<ExamSubject> subjects;
}
