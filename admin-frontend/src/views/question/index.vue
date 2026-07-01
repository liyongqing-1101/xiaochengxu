<template>
  <div class="question-page">
    <!-- 筛选栏 -->
    <el-form :inline="true" :model="filters" class="filter-form">
      <el-form-item label="科目">
        <el-select v-model="filters.subjectId" placeholder="全部科目" clearable style="width: 150px">
          <el-option
            v-for="opt in subjectOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="题型">
        <el-select v-model="filters.type" placeholder="全部题型" clearable style="width: 120px">
          <el-option label="单选题" :value="1" />
          <el-option label="多选题" :value="2" />
          <el-option label="判断题" :value="3" />
        </el-select>
      </el-form-item>

      <el-form-item label="状态">
        <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 120px">
          <el-option label="已上架" :value="1" />
          <el-option label="已下架" :value="0" />
        </el-select>
      </el-form-item>

      <el-form-item label="关键词">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索题干"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮行 -->
    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">
        ＋ 新增题目
      </el-button>
      <el-button type="success" @click="handleImport">
        📥 Excel批量导入
      </el-button>
      <el-button
        type="danger"
        :disabled="selectedIds.length === 0"
        @click="handleBatchDelete"
      >
        批量删除 ({{ selectedIds.length }})
      </el-button>
      <el-button
        :disabled="selectedIds.length === 0"
        @click="handleBatchStatus(1)"
      >
        批量上架
      </el-button>
      <el-button
        :disabled="selectedIds.length === 0"
        @click="handleBatchStatus(0)"
      >
        批量下架
      </el-button>
    </div>

    <!-- 题目表格 -->
    <el-table
      :data="list"
      v-loading="loading"
      stripe
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="subjectId" label="科目" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ getSubjectName(row.subjectId) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="题型" width="90">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="stem" label="题干" min-width="280" show-overflow-tooltip />
      <el-table-column prop="answer" label="答案" width="100">
        <template #default="{ row }">
          <span style="font-weight: bold; color: #409eff">{{ row.answer }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="explanation" label="解析" min-width="150" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            @change="(val: boolean) => handleToggleStatus(row, val)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="filters.page"
        v-model:page-size="filters.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="handlePageChange"
        @size-change="handlePageChange"
      />
    </div>

    <!-- 编辑弹窗 -->
    <QuestionEditDialog
      :visible="editVisible"
      :question="editingQuestion"
      @close="editVisible = false"
      @success="handleEditSuccess"
    />

    <!-- 导入弹窗 -->
    <ImportDialog
      :visible="importVisible"
      @close="importVisible = false"
      @success="handleImportSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import QuestionEditDialog from '@/components/question/QuestionEditDialog.vue'
import ImportDialog from '@/components/question/ImportDialog.vue'
import { questionApi } from '@/api/modules/question'
import type { ExamQuestion } from '@/types/question'

// 数据
const list = ref<ExamQuestion[]>([])
const total = ref(0)
const loading = ref(false)
const selectedIds = ref<number[]>([])
const editVisible = ref(false)
const importVisible = ref(false)
const editingQuestion = ref<ExamQuestion | null>(null)

// 筛选条件（简化版：只有科目、题型、状态、关键词）
const filters = reactive({
  subjectId: undefined as number | undefined,
  type: undefined as number | undefined,
  status: undefined as number | undefined,
  keyword: '',
  page: 1,
  pageSize: 20,
})

// 科目选项（模拟数据，实际从API获取）
const subjectOptions = [
  { value: 1, label: '高等教育学' },
  { value: 2, label: '高等教育法规' },
  { value: 3, label: '教师伦理学' },
  { value: 4, label: '大学心理学' },
]

// 获取科目名称
function getSubjectName(subjectId: number) {
  const subject = subjectOptions.find(s => s.value === subjectId)
  return subject?.label || '未知'
}

// 题型映射
function typeLabel(type: number) {
  const map: Record<number, string> = { 1: '单选', 2: '多选', 3: '判断' }
  return map[type] || '未知'
}
function typeTag(type: number) {
  const map: Record<number, string> = { 1: '', 2: 'success', 3: 'warning' }
  return map[type] || 'info'
}

// 表格多选
function handleSelectionChange(rows: ExamQuestion[]) {
  selectedIds.value = rows.map(r => r.id)
}

// 搜索 / 重置
function handleSearch() {
  filters.page = 1
  fetchList()
}
function handleReset() {
  filters.subjectId = undefined
  filters.type = undefined
  filters.status = undefined
  filters.keyword = ''
  filters.page = 1
  fetchList()
}
function handlePageChange() {
  fetchList()
}

// 获取列表
async function fetchList() {
  loading.value = true
  try {
    const result = await questionApi.getList(filters)
    list.value = result.list || []
    total.value = result.total || 0
  } catch (e: any) {
    ElMessage.error(e?.message || '获取列表失败')
  } finally {
    loading.value = false
  }
}

// 新增 / 编辑
function handleAdd() {
  editingQuestion.value = null
  editVisible.value = true
}
function handleEdit(row: ExamQuestion) {
  editingQuestion.value = row
  editVisible.value = true
}
function handleEditSuccess() {
  fetchList()
}

// 删除
async function handleDelete(row: ExamQuestion) {
  try {
    await ElMessageBox.confirm(`确定要删除题目 #${row.id} 吗？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '删除',
    })
    await questionApi.deleteById(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
    // 取消操作
  }
}

// 批量删除
async function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的题目')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 道题目吗？`, '批量删除', {
      type: 'warning',
      confirmButtonText: '删除',
    })
    await questionApi.batchDelete(selectedIds.value)
    ElMessage.success(`已删除 ${selectedIds.value.length} 道题目`)
    selectedIds.value = []
    fetchList()
  } catch {
    // 取消操作
  }
}

// 批量上下架
async function handleBatchStatus(status: number) {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择题目')
    return
  }
  const label = status === 1 ? '上架' : '下架'
  try {
    await questionApi.batchStatus(selectedIds.value, status)
    ElMessage.success(`已批量${label}`)
    selectedIds.value = []
    fetchList()
  } catch (e: any) {
    ElMessage.error(e?.message || `批量${label}失败`)
  }
}

// 单条切换状态
async function handleToggleStatus(row: ExamQuestion, val: boolean) {
  const status = val ? 1 : 0
  try {
    await questionApi.batchStatus([row.id], status)
    ElMessage.success(`已${val ? '上架' : '下架'}`)
    fetchList()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

// 导入
function handleImport() {
  importVisible.value = true
}
function handleImportSuccess() {
  fetchList()
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.question-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 16px;
}

.action-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
}
</style>
