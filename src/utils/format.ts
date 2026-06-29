/**
 * 日期/数字格式化工具
 */

/**
 * 格式化日期
 * @param date 日期对象或时间戳
 * @param format 格式化模板, 默认 YYYY-MM-DD
 */
export function formatDate(
  date: Date | number | string,
  format = 'YYYY-MM-DD',
): string {
  const d = typeof date === 'object' ? date : new Date(date)

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second)
}

/**
 * 格式化相对时间
 */
export function formatRelativeTime(date: Date | number | string): string {
  const d = typeof date === 'object' ? date : new Date(date)
  const now = Date.now()
  const diff = now - d.getTime()
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`
  return formatDate(d, 'MM-DD')
}

/**
 * 格式化秒数为 mm:ss
 */
export function formatDuration(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

/**
 * 格式化数字 (千分位)
 */
export function formatNumber(num: number): string {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 格式化百分比
 */
export function formatPercent(value: number, decimals = 1): string {
  return `${(value * 100).toFixed(decimals)}%`
}

/**
 * 获取考试倒计时文案
 * @param examDate 考试日期
 */
export function getCountdownText(examDate: Date | string): string {
  const target = typeof examDate === 'string' ? new Date(examDate) : examDate
  const now = new Date()
  const diff = target.getTime() - now.getTime()

  if (diff <= 0) return '考试已结束'

  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  return `距考试还有 ${days} 天`
}

/**
 * 获取今天的日期字符串 YYYY-MM-DD
 */
export function getTodayStr(): string {
  return formatDate(new Date(), 'YYYY-MM-DD')
}

/**
 * 获取星期文案
 */
export function getWeekdayText(date?: Date): string {
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  const d = date || new Date()
  return `星期${weekdays[d.getDay()]}`
}
