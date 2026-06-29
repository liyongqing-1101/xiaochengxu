<template>
  <div class="question-page">
    <!-- 筛选栏 -->
    <QuestionFilter
      :filters="store.filters"
      @search="store.fetchList"
      @reset="handleReset"
    />

    <!-- 操作栏 -->
    <el-card class="toolbar-card">
      <div class="flex-between">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleAdd">新增题目</el-button>
          <el-button @click="importVisible = true">批量导入</el-button>
        </div>
        <div class="toolbar-right">
          <el-button :disabled="!store.selectedIds.length" @click="store.batchStatus(store.selectedIds, 1)">批量上架</el-button>
          <el-button :disabled="!store.selectedIds.length" @click="store.batchStatus(store.selectedIds, 0)">批量下架</el-button>
          <el-button type="danger" :disabled="!store.selectedIds.length" @click="store.batchDelete(store.selectedIds)">批量删除</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table
        :data="store.list"
        v-loading="store.loading"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="stem" label="题干" min-width="280" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-html="row.stem" />
          </template>
        </el-table-column>
        <el-table-column prop="type" label="题型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">
              {{ QUESTION_TYPE_MAP[row.type] || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="diffTagType(row.difficulty)" size="small">
              {{ DIFFICULTY_MAP[row.difficulty] || row.difficulty }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="handleToggleStatus(row)"
              size="small"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除?" @confirm="store.deleteQuestion(row.id)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="store.filters.page"
          v-model:page-size="store.filters.pageSize"
          :total="store.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="store.fetchList"
          @current-change="store.fetchList"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <QuestionEditDialog
      v-model:visible="editVisible"
      :question="editingQuestion"
      @saved="handleSaved"
    />

    <!-- 批量导入弹窗 -->
    <ImportDialog
      v-model:visible="importVisible"
      @completed="handleImportCompleted"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useQuestionStore } from '@/stores/question'
import { QUESTION_TYPE_MAP, DIFFICULTY_MAP, type Question } from '@/types/question'
import QuestionFilter from '@components/question/QuestionFilter.vue'
import QuestionEditDialog from '@components/question/QuestionEditDialog.vue'
import ImportDialog from '@components/import/ImportDialog.vue'

const store = useQuestionStore()
const editVisible = ref(false)
const editingQuestion = ref<Question | null>(null)
const importVisible = ref(false)

store.fetchList()

function handleSelectionChange(rows: Question[]) {
  store.selectedIds = rows.map(r => r.id)
}

function typeTagType(type: number) {
  return { 1: '', 2: 'success', 3: 'warning' }[type] || 'info'
}

function diffTagType(d: number) {
  return { 1: 'success', 2: 'warning', 3: 'danger' }[d] || 'info'
}

function handleAdd() {
  editingQuestion.value = null
  editVisible.value = true
}

function handleEdit(row: Question) {
  editingQuestion.value = { ...row }
  editVisible.value = true
}

function handleToggleStatus(row: Question) {
  const newStatus = row.status === 1 ? 0 : 1
  store.batchStatus([row.id], newStatus)
}

function handleSaved() {
  editVisible.value = false
  store.fetchList()
}

function handleImportCompleted() {
  importVisible.value = false
  store.fetchList()
}

function handleReset() {
  store.filters.subjectId = undefined
  store.filters.type = undefined
  store.filters.difficulty = undefined
  store.filters.status = undefined
  store.filters.keyword = ''
  store.filters.page = 1
  store.fetchList()
}
</script>

<style scoped lang="scss">
.question-page {
  .toolbar-card {
    margin-bottom: 12px;
  }
  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
