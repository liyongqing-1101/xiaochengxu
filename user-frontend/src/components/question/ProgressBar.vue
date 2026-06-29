<template>
  <!-- 进度条组件 -->
  <view class="progress-bar">
    <view class="progress-bar__info" v-if="showInfo">
      <text class="progress-bar__label">{{ label }}</text>
      <text class="progress-bar__text">{{ current }}/{{ total }}</text>
    </view>
    <view class="progress-bar__track">
      <view
        class="progress-bar__fill"
        :class="{ 'progress-bar__fill--animate': animate }"
        :style="fillStyle"
      />
    </view>
    <text v-if="showPercent" class="progress-bar__percent">{{ percentText }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  current: number
  total: number
  label?: string
  color?: string
  height?: number
  showInfo?: boolean
  showPercent?: boolean
  animate?: boolean
}>(), {
  label: '',
  color: '#4A90D9',
  height: 12,
  showInfo: true,
  showPercent: false,
  animate: true,
})

const percentage = computed(() => {
  if (props.total === 0) return 0
  return Math.min((props.current / props.total) * 100, 100)
})

const percentText = computed(() => `${Math.round(percentage.value)}%`)

const fillStyle = computed(() => ({
  width: `${percentage.value}%`,
  background: props.color,
  height: `${props.height}rpx`,
}))
</script>

<style lang="scss" scoped>
.progress-bar {
  width: 100%;

  &__info {
    @include flex-between;
    margin-bottom: $spacing-xs;
  }

  &__label {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }

  &__text {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }

  &__track {
    width: 100%;
    height: 12rpx;
    background: $color-bg-gray;
    border-radius: 6rpx;
    overflow: hidden;
  }

  &__fill {
    height: 100%;
    border-radius: 6rpx;
    transition: width 0.6s ease;

    &--animate {
      transition: width 0.6s ease;
    }
  }

  &__percent {
    font-size: $font-size-xs;
    color: $color-primary;
    margin-top: $spacing-xs;
    text-align: right;
    display: block;
  }
}
</style>
