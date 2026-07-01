<template>
  <el-dialog
    v-model="dialogVisible"
    title="Excel批量导入题目"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="import-dialog">
      <!-- 模板下载 -->
      <div class="template-section">
        <el-alert
          title="导入说明"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>1. 请先下载导入模板，按格式填写数据</p>
            <p>2. 仅支持 .xlsx / .xls 格式文件</p>
            <p>3. 题型支持：1(单选)、2(多选)、3(判断)</p>
            <p>4. 选项用分号(;)分隔，如：A.xxx;B.xxx;C.xxx;D.xxx</p>
            <p>5. 判断题选项留空，答案填 T 或 F</p>
          </template>
        </el-alert>
        <div class="template-btn">
          <el-button type="primary" @click="downloadTemplate">
            📥 下载导入模板
          </el-button>
        </div>
      </div>

      <!-- 文件上传 -->
      <div class="upload-section">
        <el-upload
          ref="uploadRef"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
          :show-file-list="false"
          accept=".xlsx,.xls"
          :limit="1"
          :with-credentials="true"
        >
          <el-button type="success" :loading="uploading">
            {{ uploading ? '导入中...' : '选择文件并导入' }}
          </el-button>
          <template #tip>
            <div class="el-upload__tip">
              只能上传 Excel 文件，且不超过 10MB
            </div>
          </template>
        </el-upload>
      </div>

      <!-- 导入结果 -->
      <div v-if="result" class="result-section">
        <el-divider>导入结果</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="总条数">
            <span style="font-weight: bold">{{ result.total }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="成功">
            <span style="font-weight: bold; color: #67c23a">{{ result.success }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="失败">
            <span style="font-weight: bold; color: #f56c6c">{{ result.failed }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 失败明细 -->
        <div v-if="result.errors && result.errors.length > 0" class="error-list">
          <div class="error-title">失败明细：</div>
          <el-table :data="result.errors" max-height="250" size="small">
            <el-table-column prop="row" label="行号" width="80" />
            <el-table-column prop="error" label="错误原因" min-width="200" />
          </el-table>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" v-if="result && result.success > 0" @click="handleConfirm">
        完成
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '@/api/request'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => !val && emit('close'),
})

const uploadRef = ref()
const uploading = ref(false)
const result = ref<any>(null)

// 上传地址
const uploadUrl = (import.meta.env.VITE_API_BASE || '') + '/api/admin/import/upload'

// 请求头
const uploadHeaders = computed(() => ({
  // Session模式下，Cookie会自动携带，不需要额外设置token
}))

// 下载模板（使用 a 标签，Session Cookie 会自动携带）
function downloadTemplate() {
  const link = document.createElement('a')
  link.href = '/api/admin/questions/template'
  link.download = '题库导入模板.xlsx'
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 上传前校验
function beforeUpload(file: File) {
  const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls')
  if (!isExcel) {
    ElMessage.error('只能上传 .xlsx 或 .xls 格式的文件')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    return false
  }
  uploading.value = true
  result.value = null
  return true
}

// 上传成功
function handleUploadSuccess(response: any) {
  uploading.value = false
  if (response.code === 0) {
    result.value = response.data
    ElMessage.success(`导入成功：${response.data.success} 条`)
  } else {
    ElMessage.error(response.message || '导入失败')
  }
}

// 上传失败
function handleUploadError(err: any) {
  uploading.value = false
  ElMessage.error(err?.message || '上传失败，请稍后重试')
}

function handleClose() {
  result.value = null
  emit('close')
}

function handleConfirm() {
  emit('success')
  emit('close')
}
</script>

<style scoped lang="scss">
.import-dialog {
  .template-section {
    margin-bottom: 24px;

    .template-btn {
      margin-top: 16px;
      text-align: center;
    }
  }

  .upload-section {
    text-align: center;
    padding: 20px;
    border: 2px dashed #dcdfe6;
    border-radius: 8px;
    background: #fafafa;
  }

  .result-section {
    margin-top: 24px;

    .error-list {
      margin-top: 16px;

      .error-title {
        font-weight: bold;
        color: #f56c6c;
        margin-bottom: 8px;
      }
    }
  }
}
</style>
