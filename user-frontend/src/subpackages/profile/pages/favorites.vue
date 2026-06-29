<template>
  <!-- 我的收藏 -->
  <view class="favorites-page">
    <AppEmpty
      v-if="list.length === 0 && !loading"
      title="暂无收藏"
      description="答题时点击收藏按钮即可收藏题目"
    />
    <scroll-view v-else scroll-y class="favorites-list">
      <view v-for="question in list" :key="question.id">
        <QuestionCard :question="question" :show-options="true" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { Question } from '@/types/question'
import { useExamStore } from '@/stores/exam'
import QuestionCard from '@components/question/QuestionCard.vue'
import AppEmpty from '@components/common/AppEmpty.vue'

const examStore = useExamStore()
const list = ref<Question[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const { questionApi } = await import('@/api/modules/question')
    const result = await questionApi.getCollectedQuestions({
      page: 1,
      pageSize: 50,
      categoryId: examStore.currentCategoryId,
    })
    list.value = result.list
  } catch {
    // 静默失败
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.favorites-page {
  min-height: 100vh;
  background: $color-bg-page;
}

.favorites-list {
  padding-top: $spacing-sm;
  height: 100vh;
}
</style>
