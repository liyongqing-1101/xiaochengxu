<template>
  <!-- 倒计时组件 -->
  <view class="question-timer" :class="{ 'question-timer--warning': isWarning }">
    <image class="question-timer__icon" src="/static/images/icons/timer.png" mode="aspectFit" />
    <text class="question-timer__text">{{ display }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  display: string
  remaining: number
}>()

const isWarning = computed(() => props.remaining <= 300) // 5分钟警告
</script>

<style lang="scss" scoped>
.question-timer {
  @include flex-center;
  gap: $spacing-xs;
  padding: $spacing-xs $spacing-md;
  background: $color-bg-page;
  border-radius: $radius-xl;

  &--warning {
    background: $answer-wrong;
    .question-timer__text {
      color: $color-danger;
    }
  }

  &__icon {
    width: 28rpx;
    height: 28rpx;
  }

  &__text {
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
    color: $color-text-secondary;
    font-variant-numeric: tabular-nums;
  }
}
</style>
