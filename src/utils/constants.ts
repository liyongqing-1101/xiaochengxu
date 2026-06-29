/**
 * 全局常量定义
 * 所有魔法值集中管理，禁止在业务代码中硬编码
 */

/** 应用信息 */
export const APP = {
  NAME: '高校教资题库',
  VERSION: '1.0.0',
  /** 默认考试分类 */
  DEFAULT_CATEGORY_ID: 1,
} as const

/** API 配置 */
export const API = {
  /** 接口基础路径 */
  BASE_URL: import.meta.env.VITE_API_BASE || '',
  /** 请求超时时间(ms) */
  TIMEOUT: 15000,
  /** 重试次数 */
  RETRY_COUNT: 2,
  /** 重试间隔(ms) */
  RETRY_DELAY: 1000,
} as const

/** 本地存储 key 前缀, 避免命名冲突 */
export const STORAGE_PREFIX = 'wxjz:'

/** 存储 Key 枚举 */
export enum StorageKey {
  /** JWT Token */
  TOKEN = 'auth:token',
  /** 用户信息 */
  USER_INFO = 'auth:user_info',
  /** 当前考试分类ID */
  CATEGORY_ID = 'app:category_id',
  /** 错题列表 */
  WRONG_QUESTIONS = 'wrong:list',
  /** 答题进度(未完成会话恢复) */
  ANSWER_PROGRESS = 'answer:progress',
  /** 打卡记录 */
  CHECK_IN = 'user:check_in',
  /** 搜索历史 */
  SEARCH_HISTORY = 'app:search_history',
  /** 做题缓存 */
  QUESTION_CACHE = 'app:question_cache',
}

/** 分页配置 */
export const PAGINATION = {
  /** 默认每页条数 */
  DEFAULT_PAGE_SIZE: 20,
  /** 最大每页条数 */
  MAX_PAGE_SIZE: 50,
} as const

/** 计时器配置 */
export const TIMER = {
  /** 答题倒计时(秒), 45分钟 */
  COUNTDOWN_SECONDS: 45 * 60,
  /** 自动保存间隔(ms) */
  AUTO_SAVE_INTERVAL: 30_000,
} as const

/** 打卡配置 */
export const CHECK_IN = {
  /** 最大连续打卡天数 */
  MAX_CONSECUTIVE_DAYS: 365,
  /** 奖励图标 */
  REWARD_ICONS: ['🔥', '⭐', '💎', '🏆', '👑'] as const,
} as const

/** 缓存配置 */
export const CACHE = {
  /** 考试分类缓存 TTL(ms), 30分钟 */
  CATEGORY_TTL: 30 * 60 * 1000,
  /** 题目缓存 TTL */
  QUESTION_TTL: 10 * 60 * 1000,
  /** 用户信息缓存 TTL */
  USER_INFO_TTL: 5 * 60 * 1000,
} as const

/** 题目操作反馈文案 */
export const QUESTION_FEEDBACK = {
  CORRECT: ['太棒了! 👏', '回答正确! 🎉', '完全正确! 💯', '真厉害! 🌟'],
  INCORRECT: ['再想想哦 🤔', '差一点就对了 💪', '继续加油! 📚', '不要灰心! 🔥'],
} as const
