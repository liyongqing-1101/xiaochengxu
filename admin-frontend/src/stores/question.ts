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

  // 编辑弹窗状态
  const editVisible = ref(false)
  const editingQuestion = ref<Question | null>(null)

  const query = reactive<QuestionQuery>({
    subjectId: undefined,
    chapterId: undefined,
    tagId: undefined,
    type: undefined,
    difficulty: undefined,
    status: undefined,
    keyword: '',
    page: 1,
    pageSize: 20,
  })

  // 获取题目列表
  async function fetchList() {
    loading.value = true
    try {
      const result = await questionApi.getList(query)
      list.value = result.list || []
      total.value = result.total || 0
    } catch (e: any) {
      ElMessage.error(e?.message || '获取题目列表失败')
    } finally {
      loading.value = false
    }
  }

  // 保存题目
  async function saveQuestion(data: Partial<Question>) {
    await questionApi.save(data)
    ElMessage.success('保存成功')
    closeEditDialog()
    await fetchList()
  }

  // 删除题目
  async function deleteQuestion(id: number) {
    await questionApi.deleteById(id)
    ElMessage.success('删除成功')
    await fetchList()
  }

  // 批量删除
  async function batchDelete(ids: number[]) {
    await questionApi.batchDelete(ids)
    ElMessage.success('批量删除成功')
    selectedIds.value = []
    await fetchList()
  }

  // 批量更新状态
  async function batchStatus(ids: number[], status: number) {
    await questionApi.batchStatus(ids, status)
    const label = status === 1 ? '上架' : '下架'
    ElMessage.success(`批量${label}成功`)
    selectedIds.value = []
    await fetchList()
  }

  // 表格多选
  function setSelectedIds(ids: number[]) {
    selectedIds.value = ids
  }

  // 打开编辑弹窗
  function openEditDialog(question?: Question) {
    editingQuestion.value = question || null
    editVisible.value = true
  }

  // 关闭编辑弹窗
  function closeEditDialog() {
    editVisible.value = false
    editingQuestion.value = null
  }

  // 重置查询条件
  function resetQuery() {
    query.subjectId = undefined
    query.type = undefined
    query.status = undefined
    query.keyword = ''
    query.page = 1
  }

  // 删除单题（带确认）
  async function removeQuestion(id: number) {
    await ElMessageBox.confirm(`确定要删除题目 #${id} 吗？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '删除',
    })
    await deleteQuestion(id)
  }

  // 批量删除选中项
  async function removeSelected() {
    if (selectedIds.value.length === 0) return
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 道题目吗？`, '批量删除', {
      type: 'warning',
    })
    await batchDelete(selectedIds.value)
  }

  // 更新状态
  async function updateStatus(ids: number[], status: number) {
    await batchStatus(ids, status)
  }

  return {
    list,
    total,
    loading,
    selectedIds,
    query,
    editVisible,
    editingQuestion,
    fetchList,
    saveQuestion,
    deleteQuestion,
    batchDelete,
    batchStatus,
    setSelectedIds,
    openEditDialog,
    closeEditDialog,
    resetQuery,
    removeQuestion,
    removeSelected,
    updateStatus,
  }
})
