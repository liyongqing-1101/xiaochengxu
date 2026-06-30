package com.wxjiaozi.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 科目枚举
 * 固定4门科目，不支持动态扩展
 */
@Getter
public enum SubjectEnum {

    HIGHER_EDUCATION(1, "高等教育学"),
    EDUCATION_LAW(2, "高等教育法规和政策"),
    TEACHER_ETHICS(3, "教师伦理学"),
    PSYCHOLOGY(4, "大学心理学");

    private final Integer code;
    private final String name;

    SubjectEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据 code 获取枚举
     * @param code 科目编码
     * @return 对应的枚举，不存在返回 null
     */
    public static SubjectEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SubjectEnum subject : values()) {
            if (subject.getCode().equals(code)) {
                return subject;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取枚举，不存在抛出异常
     * @param code 科目编码
     * @return 对应的枚举
     * @throws IllegalArgumentException 非法科目编码
     */
    public static SubjectEnum getByCodeOrThrow(Integer code) {
        SubjectEnum subject = getByCode(code);
        if (subject == null) {
            throw new IllegalArgumentException("非法的科目编码: " + code + "，有效范围：1~4");
        }
        return subject;
    }

    /**
     * 校验科目编码是否合法
     * @param code 科目编码
     * @return true=合法，false=非法
     */
    public static boolean isValidCode(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 获取全部科目列表
     * @return 所有科目枚举列表
     */
    public static List<SubjectEnum> getAllSubjects() {
        return Arrays.asList(values());
    }

    /**
     * 获取全部科目名称列表
     * @return 所有科目名称列表
     */
    public static List<String> getAllSubjectNames() {
        return Arrays.stream(values())
                .map(SubjectEnum::getName)
                .collect(Collectors.toList());
    }

    /**
     * 获取全部科目编码列表
     * @return 所有科目编码列表
     */
    public static List<Integer> getAllSubjectCodes() {
        return Arrays.stream(values())
                .map(SubjectEnum::getCode)
                .collect(Collectors.toList());
    }
}
