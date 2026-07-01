package com.wxjiaozi.util;

import com.wxjiaozi.dto.QuestionOptionDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 选项列表格式转换工具
 * 用于 List<String> 与 List<QuestionOptionDTO> 之间的兼容转换
 */
public class OptionListConverter {

    /**
     * 将 List<String> 转换为 List<QuestionOptionDTO>
     * 用于：实体 -> DTO 转换
     */
    public static List<QuestionOptionDTO> toDTOList(List<String> stringList) {
        if (stringList == null || stringList.isEmpty()) {
            return null;
        }
        List<QuestionOptionDTO> result = new ArrayList<>();
        char key = 'A';
        for (String value : stringList) {
            if (value != null && !value.trim().isEmpty()) {
                QuestionOptionDTO dto = new QuestionOptionDTO();
                dto.setKey(String.valueOf(key));
                dto.setValue(value);
                result.add(dto);
                key++;
            }
        }
        return result.isEmpty() ? null : result;
    }

    /**
     * 将 List<QuestionOptionDTO> 转换为 List<String>
     * 用于：DTO -> 实体 转换
     */
    public static List<String> toStringList(List<QuestionOptionDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (QuestionOptionDTO dto : dtoList) {
            if (dto != null && dto.getValue() != null && !dto.getValue().trim().isEmpty()) {
                result.add(dto.getValue());
            }
        }
        return result.isEmpty() ? null : result;
    }
}
