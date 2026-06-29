package com.wxjiaozi.dto.mini;

import lombok.Data;

@Data
public class DailyQuestionDTO {

    private String date;
    private QuestionDTO question;
    private boolean answered;
}
