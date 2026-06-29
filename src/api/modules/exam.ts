/**
 * 考试分类 API 模块
 *
 * [EXTENSION-POINT] 新增考试分类无需修改此模块
 * 所有接口通过 categoryId 参数区分, 后端数据层新增即可
 */
import { get } from '@/utils/request'
import type { ExamCategory, Subject, Chapter, KnowledgePoint } from '@/types/exam'

export const examApi = {
  /**
   * 获取所有考试分类及科目
   * GET /exam/categories
   */
  getCategories(): Promise<ExamCategory[]> {
    return get<ExamCategory[]>('/exam/categories')
  },

  /**
   * 获取指定分类下的科目列表
   * GET /exam/subjects?categoryId=1
   */
  getSubjects(categoryId: number): Promise<Subject[]> {
    return get<Subject[]>('/exam/subjects', { categoryId })
  },

  /**
   * 获取指定科目下的章节列表
   * GET /exam/chapters?categoryId=1&subjectId=2
   */
  getChapters(categoryId: number, subjectId: number): Promise<Chapter[]> {
    return get<Chapter[]>('/exam/chapters', { categoryId, subjectId })
  },

  /**
   * 获取指定章节下的知识点列表
   * GET /exam/knowledge-points?categoryId=1&subjectId=2&chapterId=3
   */
  getKnowledgePoints(
    categoryId: number,
    subjectId: number,
    chapterId: number,
  ): Promise<KnowledgePoint[]> {
    return get<KnowledgePoint[]>('/exam/knowledge-points', {
      categoryId,
      subjectId,
      chapterId,
    })
  },
}
