package com.wxjiaozi.dto.mini;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WrongQuestionDTO {

    private Long id;
    private Long questionId;
    private QuestionDTO question;
    private Integer errorCount;
    private LocalDateTime lastErrorTime;
    private String lastWrongAnswer;
    private Integer source;
}
