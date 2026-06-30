<template>
  <van-popup
    :show="visible"
    position="bottom"
    round
    custom-style="background: #fff; border-radius: 24rpx 24rpx 0 0; padding-bottom: calc(env(safe-area-inset-bottom) + 32rpx);"
    @close="$emit('close')"
  >
    <view class="random-popup">
      <!-- 右上角关闭叉号 -->
      <view class="random-popup__close" @tap.stop="$emit('close')">
        <text class="random-popup__close-icon">✕</text>
      </view>

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
            <text class="random-popup__subject-count">
              <template v-if="selectedId === subject.id && stats">
                单选{{ stats.singleCount }} · 多选{{ stats.multiCount }} · 判断{{ stats.trueFalseCount }}
              </template>
              <template v-else>
                {{ subject.totalQuestions || '—' }} 道题目
              </template>
            </text>
          </view>
        </view>
      </view>

      <view class="random-popup__rules">
        <text class="random-popup__rules-title">试卷结构</text>
        <view class="random-popup__rule-item">
          <text class="random-popup__rule-label">单选题</text>
          <text class="random-popup__rule-value">
            40 道（1分/道）
            <text v-if="stats" class="random-popup__rule-stock">
              {{ stats.singleCount >= 40 ? '✓' : '⚠不足' }}
            </text>
          </text>
        </view>
        <view class="random-popup__rule-item">
          <text class="random-popup__rule-label">多选题</text>
          <text class="random-popup__rule-value">
            20 道（2分/道，少选/多选/错选均0分）
            <text v-if="stats" class="random-popup__rule-stock">
              {{ stats.multiCount >= 20 ? '✓' : '⚠不足' }}
            </text>
          </text>
        </view>
        <view class="random-popup__rule-item">
          <text class="random-popup__rule-label">判断题</text>
          <text class="random-popup__rule-value">
            20 道（1分/道）
            <text v-if="stats" class="random-popup__rule-stock">
              {{ stats.trueFalseCount >= 20 ? '✓' : '⚠不足' }}
            </text>
          </text>
        </view>
        <view class="random-popup__rule-item random-popup__rule-item--total">
          <text class="random-popup__rule-label">总分</text>
          <text class="random-popup__rule-value">100 分 | 限时 30 分钟</text>
        </view>
        <view
          v-if="stats && !hasEnough"
          class="random-popup__warning"
        >
          <text>⚠ 该科目题量不足，无法生成随机试卷</text>
        </view>
      </view>

      <view
        class="random-popup__btn"
        :class="{ 'random-popup__btn--disabled': !selectedId || (stats && !hasEnough) }"
        @tap="handleStart"
      >
        <text>开始刷题</text>
      </view>
    </view>
  </van-popup>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Subject } from '@/types/exam'
import type { SubjectStats } from '@/types/question'

const props = defineProps<{
  visible: boolean
  subjects: Subject[]
  selectedId: number | null
  /** 当前选中科目的题型统计（用于展示实际题量） */
  stats: SubjectStats | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'select', id: number): void
  (e: 'start'): void
}>()

/** 选中科目是否有足够题量 */
const hasEnough = computed(() => {
  if (!props.stats) return true // 未加载时不阻止
  return props.stats.singleCount >= 40 && props.stats.multiCount >= 20 && props.stats.trueFalseCount >= 20
})

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
  position: relative;

  &__close {
    position: absolute;
    top: $spacing-md;
    right: $spacing-md;
    width: 56rpx;
    height: 56rpx;
    @include flex-center;
    z-index: 1;

    &-icon {
      font-size: 36rpx;
      color: $color-text-placeholder;
      line-height: 1;
    }
  }

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

  &__rule-stock {
    font-size: $font-size-xs;
    margin-left: $spacing-xs;

    .random-popup__rule-item--total & {
      font-size: $font-size-sm;
    }
  }

  &__warning {
    background: #FFF7E6;
    border: 1rpx solid #FFD591;
    border-radius: $radius-sm;
    padding: $spacing-sm $spacing-md;
    margin-top: $spacing-sm;

    text {
      font-size: $font-size-sm;
      color: #FA8C16;
    }
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
