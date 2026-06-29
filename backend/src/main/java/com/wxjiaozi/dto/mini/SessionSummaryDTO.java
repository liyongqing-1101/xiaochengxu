package com.wxjiaozi.dto.mini;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionSummaryDTO {

    private String sessionId;
    private int totalQuestions;
    private int correctCount;
    private double accuracy;
    private long duration;
    private Long subjectId;
    private String subjectName;
    private LocalDateTime createdAt;
}
