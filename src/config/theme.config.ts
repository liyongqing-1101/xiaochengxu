/**
 * 主题配置
 * 统一管理所有主题色值、间距等 UI 常量
 */

export const THEME = {
  /** 主色调 */
  primary: '#4A90D9',
  primaryLight: '#7AB3E8',
  primaryDark: '#2E6CB5',
  primaryBg: '#EBF3FC',

  /** 功能色 */
  success: '#52C41A',
  warning: '#FAAD14',
  danger: '#FF4D4F',
  info: '#909399',

  /** 文字色 */
  textPrimary: '#1A1A1A',
  textRegular: '#333333',
  textSecondary: '#666666',
  textPlaceholder: '#999999',
  textDisabled: '#C0C4CC',

  /** 背景色 */
  bgPage: '#F5F7FA',
  bgWhite: '#FFFFFF',
  bgGray: '#F0F2F5',
  bgStriped: '#FAFBFC',

  /** 边框色 */
  border: '#E8E8E8',
  borderLight: '#F0F0F0',
  borderDark: '#D9D9D9',

  /** 答题状态色 */
  answerCorrect: '#E6F9E6',
  answerCorrectBorder: '#52C41A',
  answerWrong: '#FFF1F0',
  answerWrongBorder: '#FF4D4F',
  answerSelected: '#EBF3FC',
  answerSelectedBorder: '#4A90D9',

  /** 圆角 */
  radiusSm: 4,
  radiusBase: 8,
  radiusMd: 12,
  radiusLg: 16,
  radiusXl: 24,

  /** 间距 (rpx) */
  spacingXs: 8,
  spacingSm: 16,
  spacingMd: 24,
  spacingBase: 32,
  spacingLg: 48,
  spacingXl: 64,
} as const
