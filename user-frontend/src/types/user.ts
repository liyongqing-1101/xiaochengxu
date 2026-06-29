import type { Gender } from './enums'

/** 用户信息 */
export interface UserInfo {
  /** 用户ID */
  id: number
  /** 微信openid（用户名注册用户可能没有） */
  openid?: string
  /** 昵称 */
  nickname: string
  /** 头像URL */
  avatar: string
  /** 性别 */
  gender: Gender
  /** 手机号 */
  phone?: string
  /** 会员状态: free-免费, vip-会员 */
  membership: 'free' | 'vip'
  /** 注册时间 */
  createdAt: string
}

/** 用户统计数据 */
export interface UserStats {
  /** 总做题数 */
  totalQuestions: number
  /** 正确题数 */
  correctCount: number
  /** 正确率 */
  accuracy: number
  /** 累计打卡天数 */
  totalCheckInDays: number
  /** 连续打卡天数 */
  streakDays: number
  /** 今日是否已打卡 */
  todayCheckedIn: boolean
}

/** 打卡结果 */
export interface CheckInResult {
  /** 连续打卡天数 */
  consecutiveDays: number
  /** 今日是否已打卡 */
  todayCheckedIn: boolean
  /** 获得积分 */
  rewardPoints?: number
}

/** 登录结果 */
export interface LoginResult {
  /** JWT token */
  token: string
  /** 用户信息 */
  userInfo: UserInfo
}

/** 练习历史记录 */
export interface PracticeHistory {
  /** 记录ID */
  id: number
  /** 科目名称 */
  subjectName: string
  /** 总题数 */
  totalQuestions: number
  /** 正确数 */
  correctCount: number
  /** 正确率 */
  accuracy: number
  /** 用时(秒) */
  duration: number
  /** 练习时间 */
  createdAt: string
}
