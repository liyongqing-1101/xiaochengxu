/**
 * Pinia 入口
 */
import { createPinia } from 'pinia'

const pinia = createPinia()

export default pinia
export { useUserStore } from './user'
export { useExamStore } from './exam'
export { useQuestionStore } from './question'
export { useWrongBookStore } from './wrongBook'
