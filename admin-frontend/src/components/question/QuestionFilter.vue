<template>
  <div class="question-filter">
    <el-form :inline="true" :model="store.query" class="filter-form">
      <el-form-item label="科目">
        <el-select
          v-model="store.query.subjectId"
          placeholder="全部科目"
          clearable
        >
          <el-option
            v-for="opt in subjectOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="章节">
        <el-select
          v-model="store.query.chapterId"
          placeholder="全部章节"
          clearable
        >
          <el-option
            v-for="opt in chapterOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="知识点">
        <el-select
          v-model="store.query.tagId"
          placeholder="全部知识点"
          clearable
        >
          <el-option
            v-for="opt in tagOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="题型">
        <el-select v-model="store.query.type" placeholder="全部题型" clearable>
          <el-option
            v-for="opt in typeOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="难度">
        <el-select v-model="store.query.difficulty" placeholder="全部难度" clearable>
          <el-option
            v-for="opt in difficultyOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="关键词">
        <el-input
          v-model="store.query.keyword"
          placeholder="搜索题干"
          clearable
          @keyup.enter="$emit('search')"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="$emit('search')">搜索</el-button>
        <el-button @click="$emit('reset')">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useCategoryStore } from '@/stores/category'
import { useQuestionStore } from '@/stores/question'

const store = useQuestionStore()
const categoryStore = useCategoryStore()

defineEmits<{
  (e: 'search'): void
  (e: 'reset'): void
}>()

// 题型选项
const typeOptions = [
  { value: undefined, label: '全部题型' },
  { value: 1, label: '单选题' },
  { value: 2, label: '多选题' },
  { value: 3, label: '判断题' },
]

// 难度选项
const difficultyOptions = [
  { value: undefined, label: '全部难度' },
  { value: 1, label: '简单' },
  { value: 2, label: '中等' },
  { value: 3, label: '困难' },
]

// 从 store 获取筛选数据
const subjectOptions = computed(() => [
  { value: undefined, label: '全部科目' },
  ...categoryStore.subjects.map(s => ({ value: s.id, label: s.name })),
])

const chapterOptions = computed(() => [
  { value: undefined, label: '全部章节' },
  ...categoryStore.getChaptersBySubject(store.query.subjectId).map(c => ({ value: c.id, label: c.name })),
])

const tagOptions = computed(() => [
  { value: undefined, label: '全部知识点' },
  ...categoryStore.getTagsByChapter(store.query.chapterId).map(t => ({ value: t.id, label: t.name })),
])

// 科目变化时清空章节和知识点
watch(() => store.query.subjectId, () => {
  store.query.chapterId = undefined
  store.query.tagId = undefined
})

// 章节变化时清空知识点
watch(() => store.query.chapterId, () => {
  store.query.tagId = undefined
})

onMounted(() => {
  categoryStore.fetchCategoryTree()
})
</script>

<style scoped>
.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 12px;
  margin-right: 16px;
}
</style>