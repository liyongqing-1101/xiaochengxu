<template>
  <div class="import-page">
    <el-card>
      <el-table :data="tasks" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="fileName" label="文件名" min-width="220" />
        <el-table-column prop="categoryId" label="分类" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.categoryId === 1 ? '高校教资' : '其他' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalRows" label="总数" width="80" align="center" />
        <el-table-column prop="successCount" label="成功" width="80" align="center">
          <template #default="{ row }">
            <span style="color:#52C41A;font-weight:bold">{{ row.successCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="failCount" label="失败" width="80" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.failCount > 0 ? '#FF4D4F' : '#909399', fontWeight: 'bold' }">
              {{ row.failCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170">
          <template #default="{ row }">{{ row.createdAt }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.errorFileUrl"
              size="small"
              type="warning"
              link
              @click="downloadErrors(row.id)"
            >
              下载错误
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="fetchList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { importApi } from '@/api/modules/import'
import type { ImportTask } from '@/types/import'

const tasks = ref<ImportTask[]>([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

function statusTag(status: string) {
  const m: Record<string, string> = { PENDING: 'info', PROCESSING: 'warning', COMPLETED: 'success', FAILED: 'danger' }
  return m[status] || 'info'
}

function statusText(status: string) {
  const m: Record<string, string> = { PENDING: '等待中', PROCESSING: '处理中', COMPLETED: '已完成', FAILED: '失败' }
  return m[status] || status
}

async function fetchList() {
  loading.value = true
  try {
    const result = await importApi.getTasks(page.value, pageSize.value)
    tasks.value = result.list
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function downloadErrors(taskId: number) {
  importApi.downloadErrorFile(taskId).then((blob: any) => {
    const url = window.URL.createObjectURL(new Blob([blob]))
    const a = document.createElement('a')
    a.href = url
    a.download = `导入错误明细_${taskId}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
  })
}

onMounted(() => fetchList())
</script>

<style scoped lang="scss">
.import-page {
  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
