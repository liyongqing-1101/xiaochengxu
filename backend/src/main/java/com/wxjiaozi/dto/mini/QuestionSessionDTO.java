package com.wxjiaozi.dto.mini;

import lombok.Data;

import java.util.List;

@Data
public class QuestionSessionDTO {

    private String sessionId;
    private Long categoryId;
    private Long subjectId;
    private Integer currentIndex;
    private List<QuestionDTO> questions;
}
