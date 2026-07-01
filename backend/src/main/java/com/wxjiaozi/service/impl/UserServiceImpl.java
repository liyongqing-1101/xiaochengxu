package com.wxjiaozi.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxjiaozi.common.BusinessException;
import com.wxjiaozi.common.PageResult;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;
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
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Redis Token黑名单前缀
     */
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    public UserServiceImpl(UserMapper userMapper,
                           UserAnswerRecordMapper userAnswerRecordMapper,
                           UserCheckInMapper userCheckInMapper,
                           UserFeedbackMapper userFeedbackMapper,
                           JwtUtil jwtUtil,
                           RestTemplate restTemplate,
                           ObjectMapper objectMapper,
                           StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.userAnswerRecordMapper = userAnswerRecordMapper;
        this.userCheckInMapper = userCheckInMapper;
        this.userFeedbackMapper = userFeedbackMapper;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
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
        List<UserAnswerRecord> records = userAnswerRecordMapper.selectByUserId(userId);
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
                UserCheckIn prevCheckIn = userCheckInMapper.selectByUserIdAndDate(userId, checkDate);
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

        UserCheckIn existingCheckIn = userCheckInMapper.selectByUserIdAndDate(userId, today);
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
            UserCheckIn checkIn = userCheckInMapper.selectByUserIdAndDate(userId, checkDate);
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
        int offset = (page - 1) * pageSize;
        List<UserAnswerRecord> records = userAnswerRecordMapper.selectByUserIdWithPagination(userId, offset, pageSize);
        Long total = userAnswerRecordMapper.countByUserId(userId);

        List<PracticeHistoryDTO> list = new ArrayList<>();
        for (UserAnswerRecord record : records) {
            String sessionId = record.getSessionId();
            List<UserAnswerRecord> sessionRecords = userAnswerRecordMapper.selectBySessionIdAndUserId(sessionId, userId);

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
            dto.setCreatedAt(record.getCreatedAt());
            list.add(dto);
        }

        return new PageResult<>(list, total, page, pageSize);
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

    @Override
    public void logout(String token) {
        try {
            // 解析Token获取过期时间
            Long userId = jwtUtil.getUserIdFromToken(token);
            log.info("用户退出登录: userId={}", userId);

            // 将Token加入黑名单，设置过期时间与Token本身一致（7天）
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            // 7天 = 7 * 24 * 60 * 60 秒
            stringRedisTemplate.opsForValue().set(blacklistKey, "1", 7, TimeUnit.DAYS);

            log.info("Token已加入黑名单: userId={}", userId);
        } catch (Exception e) {
            log.warn("Token解析失败，直接加入黑名单: {}", e.getMessage());
            // 即使解析失败也加入黑名单，防止无效Token被滥用
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            stringRedisTemplate.opsForValue().set(blacklistKey, "1", 7, TimeUnit.DAYS);
        }
    }
}
