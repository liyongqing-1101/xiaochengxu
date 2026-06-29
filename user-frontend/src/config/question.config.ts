/**
 * 题目类型配置
 * [EXTENSION-POINT] 新增题型在此注册
 */

import { QuestionType } from '@/types/enums'

export interface QuestionTypeMeta {
  type: QuestionType
  label: string
  shortLabel: string
  icon: string
  color: string
  /** 最少选择数 */
  minSelect: number
  /** 最多选择数 */
  maxSelect: number
}

export const QUESTION_TYPE_REGISTRY: Record<number, QuestionTypeMeta> = {
  [QuestionType.SINGLE_CHOICE]: {
    type: QuestionType.SINGLE_CHOICE,
    label: '单选题',
    shortLabel: '单选',
    icon: 'success',
    color: '#4A90D9',
    minSelect: 1,
    maxSelect: 1,
  },
  [QuestionType.MULTI_CHOICE]: {
    type: QuestionType.MULTI_CHOICE,
    label: '多选题',
    shortLabel: '多选',
    icon: 'checked',
    color: '#FAAD14',
    minSelect: 2,
    maxSelect: 99,
  },
  [QuestionType.TRUE_FALSE]: {
    type: QuestionType.TRUE_FALSE,
    label: '判断题',
    shortLabel: '判断',
    icon: 'exchange',
    color: '#52C41A',
    minSelect: 1,
    maxSelect: 1,
  },
}

/** 获取题型元信息 */
export function getQuestionTypeMeta(type: QuestionType): QuestionTypeMeta {
  return QUESTION_TYPE_REGISTRY[type] || QUESTION_TYPE_REGISTRY[QuestionType.SINGLE_CHOICE]
}
