/**
 * API 通用类型定义
 */

/** 后端统一响应包装 */
export interface ApiResponse<T = unknown> {
  /** 业务状态码, 0 = 成功 */
  code: number
  /** 响应消息 */
  message: string
  /** 响应数据 */
  data: T
}

/** 分页结果 */
export interface PaginatedResult<T> {
  /** 数据列表 */
  list: T[]
  /** 总记录数 */
  total: number
  /** 当前页码 */
  page: number
  /** 每页条数 */
  pageSize: number
  /** 是否还有更多 */
  hasMore: boolean
}

/** 分页请求参数 */
export interface PaginatedParams {
  /** 页码, 从 1 开始 */
  page: number
  /** 每页条数 */
  pageSize: number
  /** 考试大类ID */
  categoryId: number
  /** 科目ID(可选) */
  subjectId?: number
  /** 章节ID(可选) */
  chapterId?: number
  /** 知识点ID(可选) */
  knowledgePointId?: number
  /** 搜索关键词(可选) */
  keyword?: string
}
