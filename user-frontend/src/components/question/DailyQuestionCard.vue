<template>
  <!-- 每日一题卡片 — 首页核心模块 -->
  <view class="daily-card" @tap="handleClick">
    <!-- 顶部: 日期 + 打卡按钮 -->
    <view class="daily-card__header">
      <view class="daily-card__date-row">
        <text class="daily-card__date">{{ dateText }}</text>
        <text class="daily-card__weekday">{{ weekday }}</text>
      </view>
      <view class="daily-card__check-btn" :class="{ 'daily-card__check-btn--done': answered }" @tap.stop="handleCheckIn">
        <text class="daily-card__check-text">{{ answered ? '已打卡' : '去打卡' }}</text>
      </view>
    </view>

    <!-- 题目预览 -->
    <view v-if="question" class="daily-card__question">
      <view class="daily-card__tags">
        <text class="daily-card__tag" :style="{ color: typeColor }">{{ typeLabel }}</text>
        <text class="daily-card__tag" :style="{ color: '#999' }">{{ question.subjectName }}</text>
      </view>
      <text class="daily-card__stem">{{ question.stem }}</text>
    </view>

    <!-- 无题目占位 -->
    <view v-else class="daily-card__empty">
      <text class="daily-card__empty-text">今日题目已答完, 明天再来吧~</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { DailyQuestion } from '@/types/question'
import { QUESTION_TYPE_CONFIG } from '@/types/question'
import { formatDate, getWeekdayText } from '@/utils/format'

const props = withDefaults(defineProps<{
  dailyQuestion?: DailyQuestion | null
  answered?: boolean
}>(), {
  dailyQuestion: null,
  answered: false,
})

const emit = defineEmits<{
  checkIn: []
  goAnswer: []
}>()

const question = computed(() => props.dailyQuestion?.question)
const dateText = computed(() => props.dailyQuestion?.date || formatDate(new Date(), 'MM月DD日'))
const weekday = computed(() => getWeekdayText())
const typeLabel = computed(() =>
  question.value ? QUESTION_TYPE_CONFIG[question.value.type]?.label : '',
)
const typeColor = computed(() =>
  question.value ? QUESTION_TYPE_CONFIG[question.value.type]?.color : '#999',
)

function handleCheckIn(): void {
  emit('checkIn')
}

function handleClick(): void {
  if (question.value) {
    emit('goAnswer')
  }
}
</script>

<style lang="scss" scoped>
.daily-card {
  @include card;
  margin: 0 $spacing-base $spacing-md;
  background: linear-gradient(135deg, #EBF3FC, #F5F7FA);

  &__header {
    @include flex-between;
    margin-bottom: $spacing-md;
  }

  &__date-row {
    @include flex-column;
  }

  &__date {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
  }

  &__weekday {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    margin-top: 4rpx;
  }

  &__check-btn {
    padding: $spacing-xs $spacing-md;
    background: $gradient-primary;
    border-radius: $radius-xl;
    box-shadow: 0 4rpx 12rpx rgba(74, 144, 217, 0.3);

    &--done {
      background: $color-bg-gray;
      box-shadow: none;
    }
  }

  &__check-text {
    font-size: $font-size-sm;
    color: #fff;
    font-weight: $font-weight-medium;

    .daily-card__check-btn--done & {
      color: $color-text-placeholder;
    }
  }

  &__question {
    margin-top: $spacing-sm;
  }

  &__tags {
    @include flex-start;
    gap: $spacing-xs;
    margin-bottom: $spacing-sm;
  }

  &__tag {
    font-size: $font-size-xs;
    padding: 4rpx 12rpx;
    background: rgba(255, 255, 255, 0.8);
    border-radius: $radius-sm;
  }

  &__stem {
    font-size: $font-size-base;
    color: $color-text-regular;
    line-height: $line-height-loose;
    @include text-ellipsis(2);
  }

  &__empty {
    padding: $spacing-lg 0;
    @include flex-center;
  }

  &__empty-text {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
  }
}
</style>
