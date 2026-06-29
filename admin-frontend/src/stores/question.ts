import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { questionApi } from '@/api/modules/question'
import type { Question, QuestionQuery } from '@/types/question'
import { ElMessage, ElMessageBox } from 'element-plus'

export const useQuestionStore = defineStore('question', () => {
  const list = ref<Question[]>([])
  const total = ref(0)
  const loading = ref(false)
  const selectedIds = ref<number[]>([])

  const filters = reactive<QuestionQuery>({
    categoryId: 1,
    subjectId: undefined,
    type: undefined,
    difficulty: undefined,
    status: undefined,
    keyword: '',
    page: 1,
    pageSize: 20,
  })

  async function fetchList() {
    loading.value = true
    try {
      const result = await questionApi.getList(filters)
      list.value = result.list
      total.value = result.total
    } finally {
      loading.value = false
    }
  }

  async function saveQuestion(data: Partial<Question>) {
    await questionApi.save(data)
    ElMessage.success('保存成功')
    await fetchList()
  }

  async function deleteQuestion(id: number) {
    await questionApi.deleteById(id)
    ElMessage.success('删除成功')
    await fetchList()
  }

  async function batchDelete(ids: number[]) {
    await ElMessageBox.confirm(`确认删除选中的 ${ids.length} 道题目?`, '批量删除', { type: 'warning' })
    await questionApi.batchDelete(ids)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    await fetchList()
  }

  async function batchStatus(ids: number[], status: number) {
    const label = status === 1 ? '上架' : '下架'
    await ElMessageBox.confirm(`确认${label}选中的 ${ids.length} 道题目?`, `批量${label}`, { type: 'warning' })
    await questionApi.batchStatus(ids, status)
    ElMessage.success(`批量${label}成功`)
    selectedIds.value = []
    await fetchList()
  }

  return { list, total, loading, selectedIds, filters, fetchList, saveQuestion, deleteQuestion, batchDelete, batchStatus }
})
