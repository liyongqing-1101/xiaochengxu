<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    round
    :style="{ height: '85vh' }"
    closeable
    close-icon-position="top-right"
    @close="$emit('close')"
  >
    <view class="random-exam-page">
      <!-- 标题区 -->
      <view class="random-exam-page__header">
        <text class="random-exam-page__title">随机刷题</text>
        <text class="random-exam-page__subtitle">选择科目，生成随机试卷</text>
      </view>

      <!-- 科目选择区 -->
      <view class="random-exam-page__section">
        <text class="random-exam-page__section-title">选择科目</text>
        <view class="random-exam-page__subject-list">
          <view
            v-for="subject in subjects"
            :key="subject.id"
            class="random-exam-page__subject-card"
            :class="{ 'random-exam-page__subject-card--active': selectedId === subject.id }"
            @tap="handleSelect(subject.id)"
          >
            <view
              class="random-exam-page__subject-icon"
              :style="{ background: getSubjectColor(subject.id) }"
            >
              <text class="random-exam-page__subject-emoji">{{ getSubjectIcon(subject.id) }}</text>
            </view>
            <view class="random-exam-page__subject-info">
              <text class="random-exam-page__subject-name">{{ subject.name }}</text>
              <view class="random-exam-page__subject-stats">
                <text class="random-exam-page__stat-item">
                  单选 {{ stats?.singleCount || 0 }} 道
                </text>
                <text class="random-exam-page__stat-item">
                  多选 {{ stats?.multiCount || 0 }} 道
                </text>
                <text class="random-exam-page__stat-item">
                  判断 {{ stats?.trueFalseCount || 0 }} 道
                </text>
              </view>
            </view>
            <view v-if="selectedId === subject.id" class="random-exam-page__check">✓</view>
          </view>
        </view>
      </view>

      <!-- 试卷结构说明 -->
      <view class="random-exam-page__section">
        <text class="random-exam-page__section-title">试卷结构</text>
        <view class="random-exam-page__rules">
          <view class="random-exam-page__rule-item">
            <text class="random-exam-page__rule-label">单选题</text>
            <text class="random-exam-page__rule-value">40 道 × 1 分</text>
          </view>
          <view class="random-exam-page__rule-item">
            <text class="random-exam-page__rule-label">多选题</text>
            <text class="random-exam-page__rule-value">20 道 × 2 分（少选/多选均不得分）</text>
          </view>
          <view class="random-exam-page__rule-item">
            <text class="random-exam-page__rule-label">判断题</text>
            <text class="random-exam-page__rule-value">20 道 × 1 分</text>
          </view>
          <view class="random-exam-page__rule-item random-exam-page__rule-item--total">
            <text class="random-exam-page__rule-label">总分</text>
            <text class="random-exam-page__rule-value">100 分 ｜ 限时 30 分钟</text>
          </view>
        </view>
      </view>

      <!-- 底部按钮 -->
      <view class="random-exam-page__footer">
        <view
          class="random-exam-page__btn"
          :class="{ 'random-exam-page__btn--disabled': !selectedId || !hasEnoughQuestions }"
          @tap="handleStart"
        >
          <text v-if="!selectedId">请选择科目</text>
          <text v-else-if="!hasEnoughQuestions">题量不足，无法组卷</text>
          <text v-else>开始刷题</text>
        </view>
      </view>
    </view>
  </van-popup>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Subject } from '@/types/exam'
import type { SubjectStats } from '@/types/question'

interface Props {
  visible: boolean
  subjects: Subject[]
  selectedId: number | null
  stats: SubjectStats | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'select', id: number): void
  (e: 'start'): void
}>()

const SUBJECT_ICONS: Record<number, string> = {
  1: '📖',
  2: '⚖️',
  3: '🎓',
  4: '🧠',
}

const SUBJECT_COLORS: Record<number, string> = {
  1: '#4A90D9',
  2: '#52C41A',
  3: '#FAAD14',
  4: '#FF4D4F',
}

function getSubjectIcon(id: number): string {
  return SUBJECT_ICONS[id] || '📚'
}

function getSubjectColor(id: number): string {
  return SUBJECT_COLORS[id] || '#4A90D9'
}

const hasEnoughQuestions = computed(() => {
  if (!props.stats) return true
  return props.stats.singleCount >= 40 && props.stats.multiCount >= 20 && props.stats.trueFalseCount >= 20
})

function handleSelect(id: number): void {
  emit('select', id)
}

function handleStart(): void {
  if (!props.selectedId || !hasEnoughQuestions.value) return
  emit('start')
}
</script>

<style lang="scss" scoped>
.random-exam-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: $spacing-lg;
  box-sizing: border-box;

  &__header {
    text-align: center;
    margin-bottom: $spacing-xl;
    padding-top: $spacing-sm;
  }

  &__title {
    font-size: 40rpx;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    display: block;
  }

  &__subtitle {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
    margin-top: $spacing-xs;
    display: block;
  }

  &__section {
    margin-bottom: $spacing-lg;

    &-title {
      font-size: $font-size-base;
      font-weight: $font-weight-semibold;
      color: $color-text-primary;
      margin-bottom: $spacing-md;
      display: block;
    }
  }

  &__subject-list {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
  }

  &__subject-card {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    padding: $spacing-md;
    background: $color-bg-white;
    border: 2rpx solid $color-border-light;
    border-radius: $radius-base;
    transition: all 0.2s;

    &--active {
      border-color: $color-primary;
      background: rgba(74, 144, 217, 0.05);
    }
  }

  &__subject-icon {
    width: 88rpx;
    height: 88rpx;
    border-radius: $radius-base;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__subject-emoji {
    font-size: 44rpx;
  }

  &__subject-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
  }

  &__subject-name {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  &__subject-stats {
    display: flex;
    gap: $spacing-md;
  }

  &__stat-item {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }

  &__check {
    width: 44rpx;
    height: 44rpx;
    border-radius: 50%;
    background: $color-primary;
    color: #fff;
    font-size: 28rpx;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__rules {
    background: $color-bg-white;
    border-radius: $radius-base;
    padding: $spacing-md;
  }

  &__rule-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $spacing-sm 0;

    &--total {
      border-top: 1rpx solid $color-border-light;
      margin-top: $spacing-xs;
      padding-top: $spacing-sm;

      .random-exam-page__rule-label,
      .random-exam-page__rule-value {
        font-weight: $font-weight-bold;
        color: $color-primary;
      }
    }
  }

  &__rule-label {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }

  &__rule-value {
    font-size: $font-size-sm;
    color: $color-text-regular;
  }

  &__footer {
    margin-top: auto;
    padding-top: $spacing-md;
  }

  &__btn {
    height: 96rpx;
    background: $gradient-primary;
    border-radius: $radius-xl;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8rpx 24rpx rgba(74, 144, 217, 0.3);

    text {
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
      color: #fff;
    }

    &--disabled {
      background: $color-bg-gray;
      box-shadow: none;

      text {
        color: $color-text-placeholder;
      }
    }
  }
}
</style>
