package com.wxjiaozi.util;

/**
 * Redis Key 常量工具类
 */
public class RedisKeyUtil {

    /** 答题会话前缀 */
    public static final String SESSION_PREFIX = "session:";

    /** 导入进度前缀 */
    public static final String IMPORT_PROGRESS_PREFIX = "import:progress:";

    /** 分类缓存Key */
    public static final String CATEGORY_CACHE_KEY = "cache:categories";

    /** 题目缓存前缀 */
    public static final String QUESTION_CACHE_PREFIX = "cache:question:";

    /** 会话过期时间(秒): 2小时 */
    public static final long SESSION_TTL = 7200;

    /** 导入进度过期时间(秒): 24小时 */
    public static final long IMPORT_PROGRESS_TTL = 86400;

    /**
     * 生成答题会话Redis Key
     */
    public static String sessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    /**
     * 生成导入进度Redis Key
     */
    public static String importProgressKey(Long taskId) {
        return IMPORT_PROGRESS_PREFIX + taskId;
    }
}
