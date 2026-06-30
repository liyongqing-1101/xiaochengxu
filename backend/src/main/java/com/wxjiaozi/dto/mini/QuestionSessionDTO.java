package com.wxjiaozi.dto.mini;

import lombok.Data;

import java.util.List;

@Data
public class QuestionSessionDTO {

    private String sessionId;
    private Long subjectId;
    private Integer currentIndex;
    private List<QuestionDTO> questions;
}
