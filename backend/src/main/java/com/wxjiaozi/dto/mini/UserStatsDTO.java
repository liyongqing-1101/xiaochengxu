package com.wxjiaozi.dto.mini;

import lombok.Data;

@Data
public class UserStatsDTO {

    private long totalQuestions;
    private long correctCount;
    private double accuracy;
    private int totalCheckInDays;
    private int streakDays;
    private boolean todayCheckedIn;
}
