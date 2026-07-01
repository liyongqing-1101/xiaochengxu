/** 题目类型（简化版）
 *  已移除：难度、章节、知识点相关字段
 */
export interface ExamQuestion {
  id: number
  subjectId: number
  type: 1 | 2 | 3  // 1=单选，2=多选，3=判断
  stem: string
  optionList: string[] | null  // JSON数组格式，判断题为null
  answer: string  // 单选：字母，多选：逗号分隔字母，判断：T/F
  explanation: string | null
  status: 0 | 1  // 0=禁用，1=正常
  createdAt: string
  updatedAt: string
}

/** 兼容旧版命名 */
export type Question = ExamQuestion

export const QUESTION_TYPE_MAP: Record<number, string> = {
  1: '单选',
  2: '多选',
  3: '判断',
}

export const STATUS_MAP: Record<number, string> = {
  0: '下架',
  1: '上架',
}

/** 题目查询筛选条件（简化版）
 *  已移除：难度、章节、知识点筛选
 */
export interface QuestionQuery {
  subjectId?: number
  type?: number
  status?: number
  keyword?: string
  page: number
  pageSize: number
}

/** 题目保存DTO */
export interface QuestionSaveDTO {
  id?: number
  subjectId: number
  type: number
  stem: string
  optionList?: string[] | null
  answer: string
  explanation?: string | null
  status?: number
}

/** 分页结果 */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
  hasMore: boolean
}

/** 导入结果 */
export interface ImportResult {
  total: number
  success: number
  failed: number
  errors: ImportError[]
}

/** 导入错误明细 */
export interface ImportError {
  row: number
  error: string
  data: Record<string, any>
}

/** 编辑弹窗使用的临时表单 */
export interface QuestionForm {
  id?: number
  subjectId?: number
  type: number
  stem: string
  optionA: string
  optionB: string
  optionC: string
  optionD: string
  answer: string
  explanation?: string
  status: number
}
