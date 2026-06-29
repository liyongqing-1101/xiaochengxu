<template>
  <van-popup
    :show="visible"
    position="bottom"
    round
    custom-style="background: #fff; border-radius: 24rpx 24rpx 0 0; padding-bottom: calc(env(safe-area-inset-bottom) + 32rpx);"
    @close="$emit('close')"
  >
    <view class="random-popup">
      <view class="random-popup__header">
        <text class="random-popup__title">随机刷题</text>
        <text class="random-popup__desc">选择科目，生成随机试卷</text>
      </view>

      <view class="random-popup__subjects">
        <view
          v-for="subject in subjects"
          :key="subject.id"
          class="random-popup__subject"
          :class="{ 'random-popup__subject--active': selectedId === subject.id }"
          @tap="handleSelect(subject.id)"
        >
          <view class="random-popup__radio">
            <view v-if="selectedId === subject.id" class="random-popup__radio-dot" />
          </view>
          <view class="random-popup__subject-info">
            <text class="random-popup__subject-name">{{ subject.name }}</text>
            <text class="random-popup__subject-count">{{ subject.totalQuestions }} 道题目</text>
          </view>
        </view>
      </view>

      <view class="random-popup__rules">
        <text class="random-popup__rules-title">试卷结构</text>
        <view class="random-popup__rule-item">
          <text class="random-popup__rule-label">单选题</text>
          <text class="random-popup__rule-value">40 道</text>
        </view>
        <view class="random-popup__rule-item">
          <text class="random-popup__rule-label">多选题</text>
          <text class="random-popup__rule-value">20 道（每题2分，少选/多选/错选均0分）</text>
        </view>
        <view class="random-popup__rule-item">
          <text class="random-popup__rule-label">判断题</text>
          <text class="random-popup__rule-value">20 道</text>
        </view>
        <view class="random-popup__rule-item random-popup__rule-item--total">
          <text class="random-popup__rule-label">总分</text>
          <text class="random-popup__rule-value">100 分 | 限时 30 分钟</text>
        </view>
      </view>

      <view
        class="random-popup__btn"
        :class="{ 'random-popup__btn--disabled': !selectedId }"
        @tap="handleStart"
      >
        <text>开始刷题</text>
      </view>
    </view>
  </van-popup>
</template>

<script setup lang="ts">
import type { Subject } from '@/types/exam'

defineProps<{
  visible: boolean
  subjects: Subject[]
  selectedId: number | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'select', id: number): void
  (e: 'start'): void
}>()

function handleSelect(id: number) {
  emit('select', id)
}

function handleStart() {
  emit('start')
}
</script>

<style lang="scss" scoped>
.random-popup {
  padding: $spacing-lg $spacing-base;

  &__header {
    text-align: center;
    margin-bottom: $spacing-lg;
  }

  &__title {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
  }

  &__desc {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
    margin-top: $spacing-xs;
    display: block;
  }

  &__subjects {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
    margin-bottom: $spacing-lg;
  }

  &__subject {
    @include flex-start;
    gap: $spacing-md;
    padding: $spacing-md;
    border: 2rpx solid $color-border-light;
    border-radius: $radius-base;
    transition: all 0.2s;

    &--active {
      border-color: $color-primary;
      background: $color-primary-bg;
    }
  }

  &__radio {
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    border: 2rpx solid $color-border;
    @include flex-center;
    flex-shrink: 0;

    &-dot {
      width: 24rpx;
      height: 24rpx;
      border-radius: 50%;
      background: $color-primary;
    }
  }

  &__subject-info {
    @include flex-column;
    gap: 4rpx;
  }

  &__subject-name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }

  &__subject-count {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }

  &__rules {
    background: $color-bg-page;
    border-radius: $radius-base;
    padding: $spacing-md;
    margin-bottom: $spacing-lg;

    &-title {
      font-size: $font-size-sm;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
      margin-bottom: $spacing-sm;
      display: block;
    }
  }

  &__rule-item {
    @include flex-between;
    padding: $spacing-xs 0;
    font-size: $font-size-sm;

    &--total {
      border-top: 1rpx solid $color-border-light;
      margin-top: $spacing-xs;
      padding-top: $spacing-sm;

      .random-popup__rule-label,
      .random-popup__rule-value {
        font-weight: $font-weight-bold;
        color: $color-primary;
      }
    }
  }

  &__rule-label {
    color: $color-text-secondary;
  }

  &__rule-value {
    color: $color-text-regular;
  }

  &__btn {
    @include flex-center;
    height: 88rpx;
    background: $gradient-primary;
    border-radius: $radius-xl;
    box-shadow: 0 4rpx 12rpx rgba(74, 144, 217, 0.3);

    text {
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
      color: #fff;
    }

    &--disabled {
      opacity: 0.4;
      pointer-events: none;
    }
  }
}
</style>
