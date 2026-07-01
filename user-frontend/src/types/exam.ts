import type { CategoryId } from './enums'

/** 考试大类 */
export interface ExamCategory {
  /** 分类ID */
  id: CategoryId
  /** 分类名称 */
  name: string
  /** 图标 */
  icon: string
  /** 描述 */
  description: string
  /** 科目列表 */
  subjects: Subject[]
}

/** 科目 */
export interface Subject {
  /** 科目ID */
  id: number
  /** 所属考试大类 */
  categoryId: CategoryId
  /** 科目名称, 如 "高等教育学", "大学心理学" */
  name: string
  /** 图标 */
  icon: string
  /** 总题量 */
  totalQuestions: number
  /** 用户已做题数（去重） */
  doneCount?: number
  /** 已完成题量(用户) @deprecated 使用 doneCount */
  completedQuestions?: number
  /** 章节列表 */
  chapters?: Chapter[]
}

/** 章节 */
export interface Chapter {
  /** 章节ID */
  id: number
  /** 所属科目ID */
  subjectId: number
  /** 章节名称 */
  name: string
  /** 排序 */
  order: number
  /** 题量 */
  questionCount: number
  /** 已完成题量 */
  completedCount?: number
  /** 知识点列表 */
  knowledgePoints?: KnowledgePoint[]
}

/** 知识点 */
export interface KnowledgePoint {
  /** 知识点ID */
  id: number
  /** 所属章节ID */
  chapterId: number
  /** 知识点名称 */
  name: string
  /** 题量 */
  questionCount?: number
}

/** 学习进度 */
export interface StudyProgress {
  /** 科目ID */
  subjectId: number
  /** 已完成题量 */
  completedQuestions: number
  /** 总题量 */
  totalQuestions: number
  /** 正确率 */
  accuracy: number
}
