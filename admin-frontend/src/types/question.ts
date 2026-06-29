/** 题目类型 */
export interface Question {
  id: number
  categoryId: number
  subjectId: number
  chapterId?: number
  tagId?: number
  type: 1 | 2 | 3
  stem: string
  optionA?: string
  optionB?: string
  optionC?: string
  optionD?: string
  answer: string
  explanation?: string
  difficulty: 1 | 2 | 3
  status: 0 | 1
  stemImages?: string
  createTime?: string
  updateTime?: string
}

export const QUESTION_TYPE_MAP: Record<number, string> = {
  1: '单选',
  2: '多选',
  3: '判断',
}

export const DIFFICULTY_MAP: Record<number, string> = {
  1: '简单',
  2: '中等',
  3: '困难',
}

export const STATUS_MAP: Record<number, string> = {
  0: '下架',
  1: '上架',
}

export interface QuestionQuery {
  categoryId?: number
  subjectId?: number
  chapterId?: number
  tagId?: number
  type?: number
  difficulty?: number
  status?: number
  keyword?: string
  page: number
  pageSize: number
}
