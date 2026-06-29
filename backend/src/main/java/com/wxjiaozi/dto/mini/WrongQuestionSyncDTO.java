package com.wxjiaozi.dto.mini;

import lombok.Data;

@Data
public class WrongQuestionSyncDTO {

    private Long questionId;
    private String selectedOptions;
    private Integer source;
}
