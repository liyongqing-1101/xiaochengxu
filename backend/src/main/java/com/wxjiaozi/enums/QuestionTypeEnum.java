package com.wxjiaozi.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题型枚举
 */
@Getter
public enum QuestionTypeEnum {

    SINGLE_CHOICE(1, "单选题"),
    MULTIPLE_CHOICE(2, "多选题"),
    TRUE_FALSE(3, "判断题");

    private final Integer code;
    private final String name;

    QuestionTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据 code 获取枚举
     * @param code 题型编码
     * @return 对应的枚举，不存在返回 null
     */
    public static QuestionTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (QuestionTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据 code 获取枚举，不存在抛出异常
     * @param code 题型编码
     * @return 对应的枚举
     * @throws IllegalArgumentException 非法题型编码
     */
    public static QuestionTypeEnum getByCodeOrThrow(Integer code) {
        QuestionTypeEnum type = getByCode(code);
        if (type == null) {
            throw new IllegalArgumentException("非法的题型编码: " + code + "，有效范围：1~3");
        }
        return type;
    }

    /**
     * 校验题型编码是否合法
     * @param code 题型编码
     * @return true=合法，false=非法
     */
    public static boolean isValidCode(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 获取全部题型列表
     * @return 所有题型枚举列表
     */
    public static List<QuestionTypeEnum> getAllTypes() {
        return Arrays.asList(values());
    }

    /**
     * 获取题型名称
     * @param code 题型编码
     * @return 题型名称
     */
    public static String getNameByCode(Integer code) {
        QuestionTypeEnum type = getByCode(code);
        return type != null ? type.getName() : null;
    }
}
