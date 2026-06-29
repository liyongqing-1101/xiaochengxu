/**
 * 考试分类配置注册表
 *
 * [EXTENSION-POINT] 这是整个应用的核心扩展点
 * 新增考试分类只需:
 *   1. 在 types/enums.ts 中添加 CategoryId 枚举成员
 *   2. 在此文件中添加对应的分类配置
 *   3. 后端提供对应 categoryId 的数据即可
 *
 * 无需修改任何页面代码，所有页面通过此注册表动态渲染。
 */

import { CategoryId } from '@/types/enums'

/** 分类元信息 */
export interface CategoryMeta {
  /** 分类ID */
  id: CategoryId
  /** 分类名称 */
  name: string
  /** 分类图标 */
  icon: string
  /** 分类描述 */
  description: string
  /** 主题色 */
  color: string
  /** 首页横幅图片 */
  homeBannerImage: string
  /** 分类专属题目类型, 不传则使用默认(单选/多选/判断) */
  questionTypes?: number[]
  /** 首页顶部标题 */
  homeTitle: string
}

/** 分类注册表 - 所有考试大类在此注册 */
export const CATEGORY_REGISTRY: Record<number, CategoryMeta> = {
  [CategoryId.TEACHING_QUALIFICATION]: {
    id: CategoryId.TEACHING_QUALIFICATION,
    name: '高校教资',
    icon: 'certificate',
    description: '高校教师资格证考试题库',
    color: '#4A90D9',
    homeBannerImage: '/static/images/banners/teaching.png',
    homeTitle: '教资刷题',
  },

  // [EXTENSION-POINT] 新增考试分类示例:
  // [CategoryId.COMPUTER_LEVEL_2]: {
  //   id: CategoryId.COMPUTER_LEVEL_2,
  //   name: '计算机二级',
  //   icon: 'computer',
  //   description: '全国计算机等级考试二级题库',
  //   color: '#52C41A',
  //   homeBannerImage: '/static/images/banners/computer.png',
  //   homeTitle: '计算机二级刷题',
  // },
  //
  // [CategoryId.SOFTWARE_EXAM]: {
  //   id: CategoryId.SOFTWARE_EXAM,
  //   name: '软考',
  //   icon: 'code',
  //   description: '计算机技术与软件专业技术资格考试题库',
  //   color: '#FAAD14',
  //   homeBannerImage: '/static/images/banners/software.png',
  //   homeTitle: '软考刷题',
  // },
}

/**
 * 获取分类元信息
 * @param id 分类ID
 * @returns 分类元信息, 不存在则返回默认(教资)
 */
export function getCategoryMeta(id: number): CategoryMeta {
  return CATEGORY_REGISTRY[id] || CATEGORY_REGISTRY[CategoryId.TEACHING_QUALIFICATION]
}

/**
 * 获取所有可用分类
 * @returns 分类列表
 */
export function getAvailableCategories(): CategoryMeta[] {
  return Object.values(CATEGORY_REGISTRY)
}

/**
 * 是否只有一个分类(控制分类切换按钮显隐)
 */
export function hasMultipleCategories(): boolean {
  return Object.keys(CATEGORY_REGISTRY).length > 1
}
