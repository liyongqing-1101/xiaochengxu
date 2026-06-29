<template>
  <el-card class="filter-card">
    <el-form :model="localFilters" inline>
      <el-form-item label="科目">
        <el-select v-model="localFilters.subjectId" clearable placeholder="全部科目" style="width:160px" @change="emitSearch">
          <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="题型">
        <el-select v-model="localFilters.type" clearable placeholder="全部题型" style="width:120px" @change="emitSearch">
          <el-option label="单选" :value="1" />
          <el-option label="多选" :value="2" />
          <el-option label="判断" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="难度">
        <el-select v-model="localFilters.difficulty" clearable placeholder="全部难度" style="width:120px" @change="emitSearch">
          <el-option label="简单" :value="1" />
          <el-option label="中等" :value="2" />
          <el-option label="困难" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="localFilters.status" clearable placeholder="全部状态" style="width:120px" @change="emitSearch">
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-input
          v-model="localFilters.keyword"
          placeholder="搜索题干关键词"
          clearable
          style="width:220px"
          @clear="emitSearch"
          @keyup.enter="emitSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="emitSearch">搜索</el-button>
        <el-button @click="emitReset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { categoryApi } from '@/api/modules/category'
import type { Subject } from '@/types/category'
import type { QuestionQuery } from '@/types/question'

const props = defineProps<{ filters: QuestionQuery }>()
const emit = defineEmits<{ search: []; reset: [] }>()

const subjects = ref<Subject[]>([])
const localFilters = reactive({ ...props.filters })

watch(() => props.filters, (val) => {
  Object.assign(localFilters, val)
}, { deep: true })

function emitSearch() {
  Object.assign(props.filters, localFilters)
  props.filters.page = 1
  emit('search')
}

function emitReset() {
  emit('reset')
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
.filter-card {
  margin-bottom: 12px;
}
</style>
