import type { CategoryId, QuestionType, Difficulty } from './enums'

/** 题目选项 */
export interface QuestionOption {
  /** 选项标识, 如 "A", "B", "C", "D" */
  id: string
  /** 选项标签 */
  label: string
  /** 选项内容(富文本) */
  content: string
}

/** 题目 */
export interface Question {
  /** 题目ID */
  id: number
  /** 所属考试大类 */
  categoryId: CategoryId
  /** 所属科目ID */
  subjectId: number
  /** 所属科目名称 */
  subjectName?: string
  /** 所属章节ID */
  chapterId: number
  /** 所属知识点ID */
  knowledgePointId: number
  /** 题目类型 */
  type: QuestionType
  /** 题干(富文本) */
  stem: string
  /** 选项列表 */
  options: QuestionOption[]
  /** 正确答案 */
  answer: string[]
  /** 答案解析(富文本) */
  explanation: string
  /** 难度 1-5 */
  difficulty: Difficulty
  /** 知识点标签 */
  tags: string[]
  /** 是否已收藏 */
  collected?: boolean
}

/** 用户作答记录 */
export interface UserAnswer {
  /** 题目ID */
  questionId: number
  /** 用户选择的选项 */
  selectedOptions: string[]
  /** 是否正确 */
  isCorrect: boolean
  /** 作答时间戳 */
  answeredAt: number
  /** 作答耗时(秒) */
  duration: number
}

/** 答题会话 */
export interface QuestionSession {
  /** 会话ID */
  sessionId: string
  /** 考试大类 */
  categoryId: CategoryId
  /** 科目ID */
  subjectId: number
  /** 题目列表 */
  questions: Question[]
  /** 当前题目索引 */
  currentIndex: number
  /** 用户作答记录映射 */
  answers: Record<number, UserAnswer>
  /** 开始时间 */
  startTime: number
  /** 结束时间 */
  endTime?: number
}

/** 开始答题会话参数 */
export interface StartSessionParams {
  categoryId: CategoryId
  subjectId: number
  chapterId?: number
  knowledgePointId?: number
  questionCount?: number
  /** 刷题模式: practice-练习, random-随机, exam-模拟考 */
  mode?: 'practice' | 'random' | 'exam'
  /** 随机模式 — 单选题数量 */
  singleCount?: number
  /** 随机模式 — 多选题数量 */
  multiCount?: number
  /** 随机模式 — 判断题数量 */
  trueFalseCount?: number
  /** 顺序模式 — 起始题号 */
  startFrom?: number
}

/** 提交答案参数 */
export interface SubmitAnswerParams {
  sessionId: string
  questionId: number
  selectedOptions: string[]
  duration: number
}

/** 提交答案结果 */
export interface SubmitResult {
  isCorrect: boolean
  correctAnswer: string[]
  explanation: string
  /** 知识点分析 */
  analysis?: string
}

/** 答题会话摘要 */
export interface SessionSummary {
  sessionId: string
  totalQuestions: number
  correctCount: number
  accuracy: number
  duration: number
  subjectId: number
  subjectName: string
  createdAt: string
}

/** 错题记录 */
export interface WrongQuestion {
  /** 题目信息 */
  question: Question
  /** 错误次数 */
  errorCount: number
  /** 最后错误时间 */
  lastErrorTime: number
  /** 用户错误答案 */
  lastWrongAnswer: string[]
  /** 错误来源 */
  source: import('./enums').WrongSource
}

/** 科目题目统计（按题型） */
export interface SubjectStats {
  /** 单选题数量 */
  singleCount: number
  /** 多选题数量 */
  multiCount: number
  /** 判断题数量 */
  trueFalseCount: number
  /** 总题量 */
  totalCount: number
}

/** 每日一题 */
export interface DailyQuestion {
  /** 日期 */
  date: string
  /** 题目 */
  question: Question
  /** 是否已作答 */
  answered: boolean
}

/** 题目类型显示配置 */
export const QUESTION_TYPE_CONFIG: Record<QuestionType, { label: string; icon: string; color: string }> = {
  [QuestionType.SINGLE_CHOICE]: { label: '单选题', icon: 'radio', color: '#4A90D9' },
  [QuestionType.MULTI_CHOICE]: { label: '多选题', icon: 'checkbox', color: '#FAAD14' },
  [QuestionType.TRUE_FALSE]: { label: '判断题', icon: 'swap', color: '#52C41A' },
}

/** 难度显示配置 */
export const DIFFICULTY_CONFIG: Record<Difficulty, { label: string; color: string }> = {
  [Difficulty.EASY]: { label: '简单', color: '#52C41A' },
  [Difficulty.MEDIUM]: { label: '中等', color: '#FAAD14' },
  [Difficulty.HARD]: { label: '困难', color: '#FF4D4F' },
}
