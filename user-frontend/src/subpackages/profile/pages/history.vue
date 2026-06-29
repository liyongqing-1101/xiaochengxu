<template>
  <!-- 练习记录 -->
  <view class="history-page">
    <view v-if="list.length === 0 && !loading" class="history-empty">
      <AppEmpty title="暂无练习记录" description="开始刷题后记录将显示在这里" />
    </view>
    <scroll-view v-else scroll-y class="history-list">
      <view v-for="record in list" :key="record.id" class="history-item">
        <view class="history-item__header">
          <text class="history-item__subject">{{ record.subjectName }}</text>
          <text class="history-item__time">{{ record.createdAt }}</text>
        </view>
        <view class="history-item__stats">
          <text>总题数: {{ record.totalQuestions }}</text>
          <text class="color-success">正确: {{ record.correctCount }}</text>
          <text class="color-primary">正确率: {{ (record.accuracy * 100).toFixed(1) }}%</text>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { PracticeHistory } from '@/types/user'
import { useExamStore } from '@/stores/exam'
import AppEmpty from '@components/common/AppEmpty.vue'

const examStore = useExamStore()
const list = ref<PracticeHistory[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const { userApi } = await import('@/api/modules/user')
    const result = await userApi.getHistory({
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
.history-page {
  min-height: 100vh;
  background: $color-bg-page;
}

.history-list {
  padding: $spacing-sm;
  height: 100vh;
}

.history-item {
  @include card;
  margin-bottom: $spacing-sm;

  &__header {
    @include flex-between;
    margin-bottom: $spacing-sm;
  }

  &__subject {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
  }

  &__time {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }

  &__stats {
    @include flex-start;
    gap: $spacing-md;
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}
</style>
