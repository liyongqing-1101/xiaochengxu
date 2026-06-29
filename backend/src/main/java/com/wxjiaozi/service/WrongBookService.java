package com.wxjiaozi.service;

import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.dto.mini.WrongQuestionDTO;
import com.wxjiaozi.dto.mini.WrongQuestionSyncDTO;

import java.util.List;

public interface WrongBookService {

    void syncWrongQuestions(Long userId, List<WrongQuestionSyncDTO> questions);

    PageResult<WrongQuestionDTO> getWrongList(Long userId, Long subjectId, int page, int pageSize);

    void removeWrong(Long userId, Long questionId);
}
