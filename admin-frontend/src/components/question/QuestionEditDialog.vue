<template>
  <el-dialog
    v-model="dialogVisible"
    :title="question?.id ? '编辑题目' : '新增题目'"
    width="800px"
    destroy-on-close
    @close="emit('close')"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="所属科目" prop="subjectId">
            <el-select v-model="form.subjectId" placeholder="选择科目" style="width: 100%">
              <el-option label="高等教育学" :value="1" />
              <el-option label="高等教育法规" :value="2" />
              <el-option label="教师伦理学" :value="3" />
              <el-option label="大学心理学" :value="4" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="题型" prop="type">
            <el-radio-group v-model="form.type" @change="handleTypeChange">
              <el-radio-button :value="1">单选</el-radio-button>
              <el-radio-button :value="2">多选</el-radio-button>
              <el-radio-button :value="3">判断</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="题干" prop="stem">
        <el-input
          v-model="form.stem"
          type="textarea"
          :rows="3"
          placeholder="请输入题干内容"
        />
      </el-form-item>

      <!-- 选项区域：只有单选/多选显示 -->
      <template v-if="form.type !== 3">
        <el-form-item label="选项A" prop="optionA">
          <el-input v-model="form.optionA" placeholder="选项A内容" />
        </el-form-item>
        <el-form-item label="选项B" prop="optionB">
          <el-input v-model="form.optionB" placeholder="选项B内容" />
        </el-form-item>
        <el-form-item label="选项C" prop="optionC">
          <el-input v-model="form.optionC" placeholder="选项C内容" />
        </el-form-item>
        <el-form-item label="选项D" prop="optionD">
          <el-input v-model="form.optionD" placeholder="选项D内容" />
        </el-form-item>
      </template>

      <el-form-item label="正确答案" prop="answer">
        <template v-if="form.type === 1">
          <el-radio-group v-model="form.answer">
            <el-radio value="A">A</el-radio>
            <el-radio value="B">B</el-radio>
            <el-radio value="C">C</el-radio>
            <el-radio value="D">D</el-radio>
          </el-radio-group>
        </template>
        <template v-else-if="form.type === 2">
          <el-checkbox-group v-model="answerMulti">
            <el-checkbox value="A">A</el-checkbox>
            <el-checkbox value="B">B</el-checkbox>
            <el-checkbox value="C">C</el-checkbox>
            <el-checkbox value="D">D</el-checkbox>
          </el-checkbox-group>
          <div style="color: #909399; font-size: 12px; margin-top: 8px">
            提示：按住Ctrl可多选，答案顺序按字母排序
          </div>
        </template>
        <template v-else>
          <el-radio-group v-model="form.answer">
            <el-radio value="T">正确 ✓</el-radio>
            <el-radio value="F">错误 ✗</el-radio>
          </el-radio-group>
        </template>
      </el-form-item>

      <el-form-item label="解析">
        <el-input
          v-model="form.explanation"
          type="textarea"
          :rows="2"
          placeholder="请输入答案解析（选填）"
        />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <el-button @click="emit('close')">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { questionApi } from '@/api/modules/question'
import type { ExamQuestion } from '@/types/question'

const props = defineProps<{
  visible: boolean
  question: ExamQuestion | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'success'): void
}>()

const formRef = ref<FormInstance>()
const saving = ref(false)
const answerMulti = ref<string[]>([])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => !val && emit('close'),
})

const defaultForm = () => ({
  id: undefined as number | undefined,
  subjectId: undefined as number | undefined,
  type: 1,
  stem: '',
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  answer: '',
  explanation: '',
  status: 1,
})

const form = reactive(defaultForm())

const rules: FormRules = {
  subjectId: [{ required: true, message: '请选择科目', trigger: 'change' }],
  stem: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  answer: [{ required: true, message: '请选择正确答案', trigger: 'change' }],
}

// 监听题目变化，填充表单
watch(
  () => props.question,
  (q) => {
    if (q) {
      form.id = q.id
      form.subjectId = q.subjectId
      form.type = q.type
      form.stem = q.stem
      form.explanation = q.explanation || ''
      form.status = q.status

      // 解析optionList到各选项
      if (q.optionList && Array.isArray(q.optionList)) {
        form.optionA = q.optionList[0] || ''
        form.optionB = q.optionList[1] || ''
        form.optionC = q.optionList[2] || ''
        form.optionD = q.optionList[3] || ''
      } else {
        form.optionA = ''
        form.optionB = ''
        form.optionC = ''
        form.optionD = ''
      }

      // 解析答案
      if (q.type === 2 && q.answer) {
        answerMulti.value = q.answer.split(',').filter((a: string) => a)
      } else {
        form.answer = q.answer || ''
      }
    } else {
      // 新增：重置
      Object.assign(form, defaultForm())
      answerMulti.value = []
    }
  },
  { immediate: true }
)

// 题型变化时重置答案
function handleTypeChange() {
  form.answer = ''
  answerMulti.value = []
}

// 保存
async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    // 构建optionList
    let optionList: string[] | null = null
    if (form.type !== 3) {
      optionList = []
      if (form.optionA) optionList.push(form.optionA)
      if (form.optionB) optionList.push(form.optionB)
      if (form.optionC) optionList.push(form.optionC)
      if (form.optionD) optionList.push(form.optionD)
    }

    // 构建答案（多选时用逗号分隔）
    let answer = form.type === 2 ? answerMulti.value.sort().join(',') : form.answer

    await questionApi.save({
      id: form.id,
      subjectId: form.subjectId,
      type: form.type,
      stem: form.stem,
      optionList,
      answer,
      explanation: form.explanation,
      status: form.status,
    })

    ElMessage.success('保存成功')
    emit('success')
    emit('close')
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
/* 可根据需要添加样式 */
</style>
