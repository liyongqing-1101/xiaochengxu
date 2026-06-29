package com.wxjiaozi.service;

import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.dto.mini.CheckInResultDTO;
import com.wxjiaozi.dto.mini.LoginResultDTO;
import com.wxjiaozi.dto.mini.PracticeHistoryDTO;
import com.wxjiaozi.dto.mini.RegisterDTO;
import com.wxjiaozi.dto.mini.UserStatsDTO;
import com.wxjiaozi.dto.mini.LoginResultDTO.UserInfoDTO;

public interface UserService {

    LoginResultDTO loginByWechat(String code);

    void register(RegisterDTO dto);

    LoginResultDTO loginByUsername(String username, String password);

    UserInfoDTO getUserInfo(Long userId);

    void updateUserInfo(Long userId, String nickname, String avatar);

    UserStatsDTO getStats(Long userId, Long categoryId);

    CheckInResultDTO checkIn(Long userId);

    PageResult<PracticeHistoryDTO> getHistory(Long userId, int page, int pageSize);

    void submitFeedback(Long userId, String content, String contact);
}
