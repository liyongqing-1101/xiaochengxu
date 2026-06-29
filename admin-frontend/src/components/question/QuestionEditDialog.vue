<template>
  <el-dialog
    :model-value="visible"
    :title="question?.id ? '编辑题目' : '新增题目'"
    width="860px"
    destroy-on-close
    @close="$emit('update:visible', false)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" class="question-form">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="所属科目" prop="subjectId">
            <el-select v-model="form.subjectId" placeholder="选择科目" style="width:100%">
              <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
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
        <div class="editor-wrapper">
          <QuillEditor
            v-model:content="form.stem"
            contentType="html"
            theme="snow"
            toolbar="full"
            :style="{ minHeight: '120px' }"
          />
        </div>
      </el-form-item>

      <template v-if="form.type !== 3">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="选项A" prop="optionA">
              <el-input v-model="form.optionA" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选项B" prop="optionB">
              <el-input v-model="form.optionB" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="选项C">
              <el-input v-model="form.optionC" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选项D">
              <el-input v-model="form.optionD" />
            </el-form-item>
          </el-col>
        </el-row>
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
        </template>
        <template v-else>
          <el-radio-group v-model="form.answer">
            <el-radio value="T">正确 ✓</el-radio>
            <el-radio value="F">错误 ✗</el-radio>
          </el-radio-group>
        </template>
      </el-form-item>

      <el-form-item label="解析">
        <div class="editor-wrapper">
          <QuillEditor
            v-model:content="form.explanation"
            contentType="html"
            theme="snow"
            toolbar="full"
            :style="{ minHeight: '100px' }"
          />
        </div>
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="难度">
            <el-radio-group v-model="form.difficulty">
              <el-radio :value="1">简单</el-radio>
              <el-radio :value="2">中等</el-radio>
              <el-radio :value="3">困难</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="上架" inactive-text="下架" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed, onMounted } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { useQuestionStore } from '@/stores/question'
import { categoryApi } from '@/api/modules/category'
import type { Subject } from '@/types/category'
import type { Question } from '@/types/question'
import type { FormInstance, FormRules } from 'element-plus'

const props = defineProps<{
  visible: boolean
  question: Question | null
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  saved: []
}>()

const store = useQuestionStore()
const formRef = ref<FormInstance>()
const saving = ref(false)
const subjects = ref<Subject[]>([])
const answerMulti = ref<string[]>([])

const defaultForm = () => ({
  id: undefined as number | undefined,
  categoryId: 1,
  subjectId: undefined as number | undefined,
  type: 1,
  stem: '',
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  answer: '',
  explanation: '',
  difficulty: 2,
  status: 1,
})

const form = reactive(defaultForm())

const rules: FormRules = {
  subjectId: [{ required: true, message: '请选择科目', trigger: 'change' }],
  stem: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  answer: [{ required: true, message: '请选择正确答案', trigger: 'change' }],
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.question) {
      const q = props.question
      form.id = q.id
      form.categoryId = q.categoryId
      form.subjectId = q.subjectId
      form.type = q.type
      form.stem = q.stem
      form.optionA = q.optionA || ''
      form.optionB = q.optionB || ''
      form.optionC = q.optionC || ''
      form.optionD = q.optionD || ''
      form.explanation = q.explanation || ''
      form.difficulty = q.difficulty
      form.status = q.status
      // Parse answer JSON
      try {
        const arr = JSON.parse(q.answer)
        if (form.type === 2) {
          answerMulti.value = arr
        } else {
          form.answer = arr[0] || ''
        }
      } catch {
        form.answer = q.answer
      }
    } else {
      Object.assign(form, defaultForm())
      answerMulti.value = []
    }
  }
})

function handleTypeChange() {
  form.answer = ''
  answerMulti.value = []
}

function handleSave() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      // Build answer JSON
      let answerJson: string
      if (form.type === 2) {
        answerJson = JSON.stringify(answerMulti.value)
      } else {
        answerJson = JSON.stringify([form.answer])
      }
      await store.saveQuestion({ ...form, answer: answerJson })
      emit('saved')
    } finally {
      saving.value = false
    }
  })
}

onMounted(async () => {
  try {
    const tree = await categoryApi.getTree()
    if (tree && tree.length > 0 && tree[0].subjects) {
      subjects.value = tree[0].subjects
    }
  } catch { /* ignore */ }
})
</script>

<style scoped lang="scss">
.question-form {
  max-height: 65vh;
  overflow-y: auto;
  padding-right: 8px;
}
.editor-wrapper {
  width: 100%;
}
</style>
