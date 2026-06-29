package com.wxjiaozi.dto.mini;

import lombok.Data;

@Data
public class CheckInResultDTO {

    private int consecutiveDays;
    private boolean todayCheckedIn;
    private Integer rewardPoints;
}
