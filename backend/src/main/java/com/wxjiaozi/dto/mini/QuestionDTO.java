package com.wxjiaozi.dto.mini;

import com.wxjiaozi.dto.QuestionOptionDTO;
import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {

    private Long id;
    private Long subjectId;
    private Integer type;
    private String stem;
    /**
     * 选项列表：[{"key":"A","value":"选项内容"},...]
     * 判断题此字段为 null
     */
    private List<QuestionOptionDTO> optionList;
    private String answer;
    private String explanation;
    private Integer status;
}
