<template>
  <el-dialog
    :model-value="visible"
    title="批量导入题库"
    width="680px"
    destroy-on-close
    @close="handleClose"
  >
    <el-steps :active="activeStep" finish-status="success" align-center style="margin-bottom:24px">
      <el-step title="下载模板" />
      <el-step title="上传文件" />
      <el-step title="导入进度" />
      <el-step title="完成" />
    </el-steps>

    <div class="step-content">
      <!-- Step 0: 下载模板 -->
      <div v-if="activeStep === 0" class="step-body">
        <el-alert title="操作说明" type="info" :closable="false" style="margin-bottom:16px">
          <p>1. 下载标准Excel模板，按模板格式填写题目数据</p>
          <p>2. 单文件最大 100MB，最多 10 万条数据</p>
          <p>3. 模板中包含填写说明，请仔细阅读</p>
        </el-alert>
        <el-form label-width="100px">
          <el-form-item label="目标分类">
            <el-select v-model="importCategoryId" style="width:200px">
              <el-option label="高校教资" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="downloadTemplate">下载模板</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- Step 1: 上传文件 -->
      <div v-if="activeStep === 1" class="step-body">
        <el-upload
          ref="uploadRef"
          drag
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.xls"
          :on-change="handleFileChange"
          :before-upload="beforeUpload"
          :file-list="fileList"
        >
          <el-icon :size="48"><UploadFilled /></el-icon>
          <div class="upload-text">将Excel文件拖到此处，或<span>点击上传</span></div>
          <template #tip>
            <div class="upload-tip">仅支持 .xlsx / .xls 格式，单文件最大 100MB</div>
          </template>
        </el-upload>
        <div v-if="selectedFile" class="file-info">
          <el-tag type="info" closable @close="selectedFile = null; fileList = []">
            {{ selectedFile.name }} ({{ formatFileSize(selectedFile.size) }})
          </el-tag>
        </div>
      </div>

      <!-- Step 2: 进度 -->
      <div v-if="activeStep === 2" class="step-body">
        <el-progress
          :percentage="progress.percent"
          :status="progressPercentStatus"
          :stroke-width="22"
          :text-inside="true"
        />
        <div class="progress-detail">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="已处理">{{ progress.currentRow }} / {{ progress.totalRows }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="progressStatusTag">{{ progressStatusText }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="成功">
              <span style="color:#52C41A;font-weight:bold">{{ progress.successCount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="失败">
              <span style="color:#FF4D4F;font-weight:bold">{{ progress.failCount }}</span>
            </el-descriptions-item>
          </el-descriptions>
          <div v-if="progress.errors?.length" class="error-list">
            <p class="error-title">错误信息:</p>
            <p v-for="(err, i) in progress.errors.slice(0, 8)" :key="i" class="error-item">{{ err }}</p>
          </div>
        </div>
      </div>

      <!-- Step 3: 完成 -->
      <div v-if="activeStep === 3" class="step-body">
        <el-result
          :icon="progress.status === 'COMPLETED' ? 'success' : 'error'"
          :title="progress.status === 'COMPLETED' ? '导入完成' : '导入失败'"
          :sub-title="`成功 ${progress.successCount} 条，失败 ${progress.failCount} 条`"
        >
          <template #extra>
            <el-button v-if="progress.failCount > 0" type="warning" @click="downloadErrors">下载错误明细</el-button>
          </template>
        </el-result>
      </div>
    </div>

    <template #footer>
      <el-button v-if="activeStep === 1" type="primary" :loading="uploading" @click="startUpload">开始导入</el-button>
      <el-button v-if="activeStep === 0" type="primary" @click="activeStep = 1">下一步</el-button>
      <el-button v-if="activeStep === 3" type="primary" @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { importApi } from '@/api/modules/import'
import type { ImportProgress } from '@/types/import'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ 'update:visible': [boolean]; completed: [] }>()

const activeStep = ref(0)
const importCategoryId = ref(1)
const selectedFile = ref<File | null>(null)
const fileList = ref<any[]>([])
const uploading = ref(false)
const taskId = ref<number>(0)
let pollingTimer: ReturnType<typeof setInterval> | null = null

const progress = ref<ImportProgress>({
  taskId: 0, totalRows: 0, currentRow: 0, successCount: 0, failCount: 0, percent: 0, status: 'PENDING', errors: [],
})

const progressPercentStatus = computed(() => {
  if (progress.value.status === 'COMPLETED') return 'success'
  if (progress.value.status === 'FAILED') return 'exception'
  return ''
})

const progressStatusTag = computed(() => {
  const s = progress.value.status
  if (s === 'COMPLETED') return 'success'
  if (s === 'FAILED') return 'danger'
  return 'warning'
})

const progressStatusText = computed(() => {
  const m: Record<string, string> = { PENDING: '等待中', PROCESSING: '处理中', COMPLETED: '已完成', FAILED: '失败' }
  return m[progress.value.status] || progress.value.status
})

function formatFileSize(bytes: number) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function downloadTemplate() {
  importApi.downloadTemplate().then((blob: any) => {
    const url = window.URL.createObjectURL(new Blob([blob]))
    const a = document.createElement('a')
    a.href = url
    a.download = '题库导入模板.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
  })
}

function handleFileChange(file: any) {
  selectedFile.value = file.raw
}

function beforeUpload(file: File) {
  const isExcel = /\.(xlsx|xls)$/i.test(file.name)
  if (!isExcel) {
    ElMessage.error('仅支持 .xlsx 或 .xls 格式')
    return false
  }
  if (file.size > 100 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过 100MB')
    return false
  }
  return true
}

async function startUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  uploading.value = true
  try {
    const result = await importApi.uploadFile(selectedFile.value, importCategoryId.value)
    taskId.value = result.taskId
    activeStep.value = 2
    startPolling()
  } catch {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

function startPolling() {
  pollingTimer = setInterval(async () => {
    try {
      const p = await importApi.getProgress(taskId.value)
      progress.value = p
      if (p.status === 'COMPLETED' || p.status === 'FAILED') {
        stopPolling()
        activeStep.value = 3
        emit('completed')
      }
    } catch { /* ignore polling errors */ }
  }, 2000)
}

function stopPolling() {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

function downloadErrors() {
  importApi.downloadErrorFile(taskId.value).then((blob: any) => {
    const url = window.URL.createObjectURL(new Blob([blob]))
    const a = document.createElement('a')
    a.href = url
    a.download = `导入错误明细_${taskId.value}.xlsx`
    a.click()
    window.URL.revokeObjectURL(url)
  })
}

function handleClose() {
  stopPolling()
  emit('update:visible', false)
  // Reset
  activeStep.value = 0
  selectedFile.value = null
  fileList.value = []
  progress.value = { taskId: 0, totalRows: 0, currentRow: 0, successCount: 0, failCount: 0, percent: 0, status: 'PENDING', errors: [] }
}

onUnmounted(() => stopPolling())
</script>

<style scoped lang="scss">
.step-body {
  min-height: 200px;
}
.upload-text {
  font-size: 14px;
  color: #606266;
  margin-top: 8px;
  span { color: #4A90D9; }
}
.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.file-info {
  margin-top: 12px;
}
.progress-detail {
  margin-top: 20px;
}
.error-list {
  margin-top: 12px;
  padding: 8px 12px;
  background: #FFF7E6;
  border-radius: 4px;
}
.error-title {
  color: #FAAD14;
  font-weight: bold;
  margin-bottom: 4px;
}
.error-item {
  color: #606266;
  font-size: 12px;
  line-height: 1.8;
}
</style>
