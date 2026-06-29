package com.wxjiaozi.dto.mini;

import lombok.Data;

import java.util.List;

@Data
public class SubmitResultDTO {

    private boolean isCorrect;
    private List<String> correctAnswer;
    private String explanation;
    private String analysis;
}
