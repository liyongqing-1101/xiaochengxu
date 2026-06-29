/**
 * 全局枚举定义
 *
 * [EXTENSION-POINT] 新增考试分类在此添加枚举成员
 * [EXTENSION-POINT] 新增题型在此添加枚举成员
 */

/** 考试大类 */
export enum CategoryId {
  /** 高校教师资格证 */
  TEACHING_QUALIFICATION = 1,
  // [EXTENSION-POINT] 新增考试分类:
  // COMPUTER_LEVEL_2 = 2,   // 全国计算机等级考试二级
  // SOFTWARE_EXAM = 3,       // 软考
}

/** 题目类型 */
export enum QuestionType {
  /** 单选题 */
  SINGLE_CHOICE = 1,
  /** 多选题 */
  MULTI_CHOICE = 2,
  /** 判断题 */
  TRUE_FALSE = 3,
  // [EXTENSION-POINT] 新增题型:
  // FILL_BLANK = 4,          // 填空题
  // SHORT_ANSWER = 5,        // 简答题
}

/** 作答状态 */
export enum AnswerStatus {
  /** 未作答 */
  UNANSWERED = 0,
  /** 已作答 */
  ANSWERED = 1,
  /** 回答正确 */
  CORRECT = 2,
  /** 回答错误 */
  INCORRECT = 3,
}

/** 题目难度 */
export enum Difficulty {
  EASY = 1,
  MEDIUM = 2,
  HARD = 3,
}

/** 用户性别 */
export enum Gender {
  UNKNOWN = 0,
  MALE = 1,
  FEMALE = 2,
}

/** 错题来源 */
export enum WrongSource {
  /** 练习模式 */
  PRACTICE = 1,
  /** 模拟考试 */
  EXAM = 2,
  /** 每日一题 */
  DAILY = 3,
}
