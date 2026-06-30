package com.wxjiaozi.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxjiaozi.common.BusinessException;
import com.wxjiaozi.common.PageResult;
import com.wxjiaozi.dto.mini.CheckInResultDTO;
import com.wxjiaozi.dto.mini.LoginResultDTO;
import com.wxjiaozi.dto.mini.LoginResultDTO.UserInfoDTO;
import com.wxjiaozi.dto.mini.PracticeHistoryDTO;
import com.wxjiaozi.dto.mini.RegisterDTO;
import com.wxjiaozi.dto.mini.UserStatsDTO;
import com.wxjiaozi.entity.User;
import com.wxjiaozi.entity.UserAnswerRecord;
import com.wxjiaozi.entity.UserCheckIn;
import com.wxjiaozi.entity.UserFeedback;
import com.wxjiaozi.mapper.UserAnswerRecordMapper;
import com.wxjiaozi.mapper.UserCheckInMapper;
import com.wxjiaozi.mapper.UserFeedbackMapper;
import com.wxjiaozi.mapper.UserMapper;
import com.wxjiaozi.security.JwtUtil;
import com.wxjiaozi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserAnswerRecordMapper userAnswerRecordMapper;
    private final UserCheckInMapper userCheckInMapper;
    private final UserFeedbackMapper userFeedbackMapper;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public UserServiceImpl(UserMapper userMapper,
                           UserAnswerRecordMapper userAnswerRecordMapper,
                           UserCheckInMapper userCheckInMapper,
                           UserFeedbackMapper userFeedbackMapper,
                           JwtUtil jwtUtil,
                           RestTemplate restTemplate,
                           ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.userAnswerRecordMapper = userAnswerRecordMapper;
        this.userCheckInMapper = userCheckInMapper;
        this.userFeedbackMapper = userFeedbackMapper;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public LoginResultDTO loginByWechat(String code) {
        String appId = "YOUR_APPID";
        String secret = "YOUR_SECRET";
        String wxUrl = "https://api.weixin.qq.com/sns/jscode2session"
                + "?appid=" + appId
                + "&secret=" + secret
                + "&js_code=" + code
                + "&grant_type=authorization_code";

        String response;
        try {
            response = restTemplate.getForObject(wxUrl, String.class);
        } catch (Exception e) {
            log.error("调用微信API失败", e);
            throw new BusinessException("微信登录失败，请稍后重试");
        }

        if (StrUtil.isBlank(response)) {
            throw new BusinessException("微信登录失败，未获取到响应");
        }

        Map<String, Object> wxResult;
        try {
            wxResult = objectMapper.readValue(response, Map.class);
        } catch (JsonProcessingException e) {
            log.error("解析微信响应失败: {}", response, e);
            throw new BusinessException("微信登录失败，响应解析异常");
        }

        if (wxResult.containsKey("errcode") && (int) wxResult.get("errcode") != 0) {
            String errmsg = (String) wxResult.getOrDefault("errmsg", "unknown error");
            log.error("微信返回错误: {} - {}", wxResult.get("errcode"), errmsg);
            throw new BusinessException("微信登录失败: " + errmsg);
        }

        String openid = (String) wxResult.get("openid");
        if (StrUtil.isBlank(openid)) {
            throw new BusinessException("微信登录失败，未获取到openid");
        }

        User user = userMapper.selectByOpenid(openid);
        if (user == null) {
            throw new BusinessException("请先注册账号");
        }

        String token = jwtUtil.generateToken(user.getId(), openid, "USER");
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setId(user.getId());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setGender(user.getGender());
        userInfo.setPhone(user.getPhone());
        userInfo.setMembership(user.getMembership());

        LoginResultDTO result = new LoginResultDTO();
        result.setToken(token);
        result.setRefreshToken(refreshToken);
        result.setUserInfo(userInfo);
        return result;
    }

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        // 校验密码长度 8-18 位
        if (dto.getPassword().length() < 8 || dto.getPassword().length() > 18) {
            throw new BusinessException("密码长度需为8-18位");
        }

        // 校验两次密码一致
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次密码不一致");
        }

        // 校验用户名唯一
        User existingUser = userMapper.selectByUsername(dto.getUsername());
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // BCrypt 加密密码
        String hashedPassword = BCrypt.hashpw(dto.getPassword());

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(hashedPassword);
        user.setNickname(dto.getUsername());
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public LoginResultDTO loginByUsername(String username, String password) {
        // 查用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // 验证密码
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 生成 JWT
        String token = jwtUtil.generateToken(user.getId(), user.getOpenid(), "USER");
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setId(user.getId());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setGender(user.getGender());
        userInfo.setPhone(user.getPhone());
        userInfo.setMembership(user.getMembership());

        LoginResultDTO result = new LoginResultDTO();
        result.setToken(token);
        result.setRefreshToken(refreshToken);
        result.setUserInfo(userInfo);
        return result;
    }

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserInfoDTO userInfo = new UserInfoDTO();
        userInfo.setId(user.getId());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setGender(user.getGender());
        userInfo.setPhone(user.getPhone());
        userInfo.setMembership(user.getMembership());
        return userInfo;
    }

    @Override
    @Transactional
    public void updateUserInfo(Long userId, String nickname, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (StrUtil.isNotBlank(nickname)) {
            user.setNickname(nickname);
        }
        if (StrUtil.isNotBlank(avatar)) {
            user.setAvatar(avatar);
        }
        userMapper.updateById(user);
    }

    @Override
    public UserStatsDTO getStats(Long userId, Long categoryId) {
        LambdaQueryWrapper<UserAnswerRecord> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(UserAnswerRecord::getUserId, userId);
        if (categoryId != null) {
            // 移除 categoryId 过滤，只按 userId 过滤
        }

        List<UserAnswerRecord> records = userAnswerRecordMapper.selectList(answerWrapper);
        long totalQuestions = records.size();
        long correctCount = records.stream().filter(r -> r.getIsCorrect() != null && r.getIsCorrect() == 1).count();
        double accuracy = totalQuestions > 0 ? (double) correctCount / totalQuestions : 0.0;

        int totalCheckInDays = userCheckInMapper.countByUserId(userId);

        UserCheckIn latestCheckIn = userCheckInMapper.selectLatestByUserId(userId);
        LocalDate today = LocalDate.now();
        boolean todayCheckedIn = latestCheckIn != null && latestCheckIn.getCheckDate().equals(today);

        int streakDays = 0;
        if (latestCheckIn != null) {
            LocalDate checkDate = latestCheckIn.getCheckDate();
            while (checkDate != null) {
                UserCheckIn prevCheckIn = userCheckInMapper.selectByUserIdAndDate(userId, checkDate.toString());
                if (prevCheckIn != null) {
                    streakDays++;
                    checkDate = checkDate.minusDays(1);
                } else {
                    break;
                }
            }
        }

        UserStatsDTO dto = new UserStatsDTO();
        dto.setTotalQuestions(totalQuestions);
        dto.setCorrectCount(correctCount);
        dto.setAccuracy(Math.round(accuracy * 10000.0) / 10000.0);
        dto.setTotalCheckInDays(totalCheckInDays);
        dto.setStreakDays(streakDays);
        dto.setTodayCheckedIn(todayCheckedIn);
        return dto;
    }

    @Override
    @Transactional
    public CheckInResultDTO checkIn(Long userId) {
        LocalDate today = LocalDate.now();
        String todayStr = today.toString();

        UserCheckIn existingCheckIn = userCheckInMapper.selectByUserIdAndDate(userId, todayStr);
        boolean todayCheckedIn = existingCheckIn != null;

        if (!todayCheckedIn) {
            UserCheckIn checkIn = new UserCheckIn();
            checkIn.setUserId(userId);
            checkIn.setCheckDate(today);
            userCheckInMapper.insert(checkIn);
        }

        int consecutiveDays = 0;
        LocalDate checkDate = today;
        while (true) {
            UserCheckIn checkIn = userCheckInMapper.selectByUserIdAndDate(userId, checkDate.toString());
            if (checkIn != null) {
                consecutiveDays++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }

        CheckInResultDTO dto = new CheckInResultDTO();
        dto.setConsecutiveDays(consecutiveDays);
        dto.setTodayCheckedIn(true);
        dto.setRewardPoints(consecutiveDays <= 7 ? consecutiveDays * 10 : 70 + (consecutiveDays - 7) * 5);
        return dto;
    }

    @Override
    public PageResult<PracticeHistoryDTO> getHistory(Long userId, int page, int pageSize) {
        LambdaQueryWrapper<UserAnswerRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAnswerRecord::getUserId, userId);
        wrapper.orderByDesc(UserAnswerRecord::getCreateTime);
        wrapper.groupBy(UserAnswerRecord::getSessionId);

        Page<UserAnswerRecord> mpPage = new Page<>(page, pageSize);
        IPage<UserAnswerRecord> result = userAnswerRecordMapper.selectPage(mpPage, wrapper);

        List<PracticeHistoryDTO> list = new ArrayList<>();
        for (UserAnswerRecord record : result.getRecords()) {
            String sessionId = record.getSessionId();
            LambdaQueryWrapper<UserAnswerRecord> sessionWrapper = new LambdaQueryWrapper<>();
            sessionWrapper.eq(UserAnswerRecord::getSessionId, sessionId);
            sessionWrapper.eq(UserAnswerRecord::getUserId, userId);
            List<UserAnswerRecord> sessionRecords = userAnswerRecordMapper.selectList(sessionWrapper);

            int totalQuestions = sessionRecords.size();
            int correctCount = (int) sessionRecords.stream()
                    .filter(r -> r.getIsCorrect() != null && r.getIsCorrect() == 1).count();
            double accuracy = totalQuestions > 0 ? (double) correctCount / totalQuestions : 0.0;
            long totalDuration = sessionRecords.stream()
                    .filter(r -> r.getDuration() != null)
                    .mapToLong(UserAnswerRecord::getDuration).sum();

            PracticeHistoryDTO dto = new PracticeHistoryDTO();
            dto.setId(record.getId());
            dto.setSubjectName("科目");
            dto.setTotalQuestions(totalQuestions);
            dto.setCorrectCount(correctCount);
            dto.setAccuracy(Math.round(accuracy * 10000.0) / 10000.0);
            dto.setDuration(totalDuration);
            dto.setCreatedAt(record.getCreateTime());
            list.add(dto);
        }

        return new PageResult<>(list, result.getTotal(), page, pageSize,
                (long) page * pageSize < result.getTotal());
    }

    @Override
    @Transactional
    public void submitFeedback(Long userId, String content, String contact) {
        UserFeedback feedback = new UserFeedback();
        feedback.setUserId(userId);
        feedback.setContent(content);
        feedback.setContact(contact);
        feedback.setStatus(0);
        userFeedbackMapper.insert(feedback);
    }
}
