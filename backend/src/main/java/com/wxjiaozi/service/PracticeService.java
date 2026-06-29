package com.wxjiaozi.service;

import com.wxjiaozi.dto.mini.EndSessionDTO;
import com.wxjiaozi.dto.mini.QuestionSessionDTO;
import com.wxjiaozi.dto.mini.SessionSummaryDTO;
import com.wxjiaozi.dto.mini.StartSessionDTO;
import com.wxjiaozi.dto.mini.SubmitAnswerDTO;
import com.wxjiaozi.dto.mini.SubmitResultDTO;

public interface PracticeService {

    QuestionSessionDTO startSession(Long userId, StartSessionDTO params);

    SubmitResultDTO submitAnswer(Long userId, SubmitAnswerDTO params);

    SessionSummaryDTO endSession(Long userId, EndSessionDTO params);
}
