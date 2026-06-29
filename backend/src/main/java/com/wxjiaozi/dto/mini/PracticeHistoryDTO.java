package com.wxjiaozi.dto.mini;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PracticeHistoryDTO {

    private Long id;
    private String subjectName;
    private int totalQuestions;
    private int correctCount;
    private double accuracy;
    private long duration;
    private LocalDateTime createdAt;
}
