<template>
  <div class="question-page">
    <!-- 筛选栏 -->
    <QuestionFilter @search="handleSearch" @reset="handleReset" />

    <!-- 操作按钮行 -->
    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">
        ＋ 新增题目
      </el-button>
      <el-button
        type="danger"
        :disabled="questionStore.selectedIds.length === 0"
        @click="handleBatchDelete"
      >
        批量删除 ({{ questionStore.selectedIds.length }})
      </el-button>
      <el-button
        :disabled="questionStore.selectedIds.length === 0"
        @click="handleBatchStatus(1)"
      >
        批量上架
      </el-button>
      <el-button
        :disabled="questionStore.selectedIds.length === 0"
        @click="handleBatchStatus(0)"
      >
        批量下架
      </el-button>
    </div>

    <!-- 题目表格 -->
    <el-table
      :data="questionStore.list"
      v-loading="questionStore.loading"
      stripe
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="stem" label="题干" min-width="280" show-overflow-tooltip />
      <el-table-column prop="type" label="题型" width="90">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="difficulty" label="难度" width="80">
        <template #default="{ row }">
          <el-tag :type="diffTag(row.difficulty)" size="small">{{ diffLabel(row.difficulty) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            @change="(val: boolean) => handleToggleStatus(row, val)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
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
        v-model:current-page="questionStore.query.page"
        v-model:page-size="questionStore.query.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="questionStore.total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="handlePageChange"
        @size-change="handlePageChange"
      />
    </div>

    <!-- 编辑弹窗 -->
    <QuestionEditDialog
      :visible="questionStore.editVisible"
      :question="questionStore.editingQuestion"
      @close="questionStore.closeEditDialog()"
      @success="handleEditSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import QuestionFilter from '@/components/question/QuestionFilter.vue'
import QuestionEditDialog from '@/components/question/QuestionEditDialog.vue'
import { useQuestionStore } from '@/stores/question'
import type { Question } from '@/types/question'

const questionStore = useQuestionStore()

// 题型和难度映射
function typeLabel(type: number) {
  const map: Record<number, string> = { 1: '单选', 2: '多选', 3: '判断' }
  return map[type] || '未知'
}
function typeTag(type: number) {
  const map: Record<number, string> = { 1: '', 2: 'success', 3: 'warning' }
  return map[type] || 'info'
}
function diffLabel(d: number) {
  const map: Record<number, string> = { 1: '简单', 2: '中等', 3: '困难' }
  return map[d] || '未知'
}
function diffTag(d: number) {
  const map: Record<number, string> = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[d] || 'info'
}

// 表格多选
function handleSelectionChange(rows: Question[]) {
  questionStore.setSelectedIds(rows.map(r => r.id))
}

// 搜索 / 重置
function handleSearch() {
  questionStore.query.page = 1
  questionStore.fetchQuestions()
}
function handleReset() {
  questionStore.resetQuery()
  questionStore.fetchQuestions()
}
function handlePageChange() {
  questionStore.fetchQuestions()
}

// 新增 / 编辑
function handleAdd() {
  questionStore.openEditDialog()
}
function handleEdit(row: Question) {
  questionStore.openEditDialog(row)
}
function handleEditSuccess() {
  questionStore.fetchQuestions()
}

// 删除
function handleDelete(row: Question) {
  ElMessageBox.confirm(`确定要删除题目 #${row.id} 吗？`, '确认删除', {
    type: 'warning',
    confirmButtonText: '删除',
  }).then(async () => {
    try {
      await questionStore.removeQuestion(row.id)
      ElMessage.success('删除成功')
    } catch (e: any) {
      ElMessage.error(e?.message || '删除失败')
    }
  }).catch(() => {})
}

// 批量删除
function handleBatchDelete() {
  const count = questionStore.selectedIds.length
  ElMessageBox.confirm(`确定要删除选中的 ${count} 道题目吗？`, '批量删除', {
    type: 'warning',
    confirmButtonText: '删除',
  }).then(async () => {
    try {
      await questionStore.removeSelected()
      ElMessage.success(`已删除 ${count} 道题目`)
    } catch (e: any) {
      ElMessage.error(e?.message || '批量删除失败')
    }
  }).catch(() => {})
}

// 批量上下架
async function handleBatchStatus(status: number) {
  const label = status === 1 ? '上架' : '下架'
  try {
    await questionStore.updateStatus(questionStore.selectedIds, status)
    ElMessage.success(`已批量${label}`)
  } catch (e: any) {
    ElMessage.error(e?.message || `批量${label}失败`)
  }
}

// 单条切换状态
async function handleToggleStatus(row: Question, val: boolean) {
  try {
    await questionStore.updateStatus([row.id], val ? 1 : 0)
    ElMessage.success(`已${val ? '上架' : '下架'}`)
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(() => {
  questionStore.fetchQuestions()
})
</script>

<style scoped>
.question-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
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