<template>
  <!-- 答题卡弹窗 — 快速切换题目 -->
  <van-popup
    :show="visible"
    position="bottom"
    round
    @close="$emit('close')"
  >
    <view class="answer-sheet">
      <!-- 标题 -->
      <view class="answer-sheet__header">
        <text class="answer-sheet__title">答题卡</text>
        <text class="answer-sheet__close" @tap="$emit('close')">✕</text>
      </view>

      <!-- 统计 -->
      <view class="answer-sheet__stats">
        <view class="answer-sheet__stat">
          <view class="answer-sheet__dot answer-sheet__dot--answered" />
          <text>已答 {{ answeredCount }}</text>
        </view>
        <view class="answer-sheet__stat">
          <view class="answer-sheet__dot answer-sheet__dot--unanswered" />
          <text>未答 {{ unansweredCount }}</text>
        </view>
      </view>

      <!-- 题目网格 -->
      <view class="answer-sheet__grid">
        <view
          v-for="(status, questionId) in statusMap"
          :key="questionId"
          class="answer-sheet__item"
          :class="getItemClass(status)"
          @tap="handleJump(questionId)"
        >
          <text class="answer-sheet__num">{{ getQuestionIndex(questionId) }}</text>
        </view>
      </view>
    </view>
  </van-popup>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { AnswerStatus } from '@/types/enums'

const props = defineProps<{
  visible: boolean
  statusMap: Record<number, AnswerStatus>
  questionOrder: number[]  // 题目ID列表(保持顺序)
}>()

const emit = defineEmits<{
  close: []
  jump: [questionId: number]
}>()

const totalCount = computed(() => props.questionOrder.length)
const answeredCount = computed(() =>
  Object.values(props.statusMap).filter(s => s !== AnswerStatus.UNANSWERED).length,
)
const unansweredCount = computed(() => totalCount.value - answeredCount.value)

function getQuestionIndex(questionId: number): number {
  return props.questionOrder.indexOf(questionId) + 1
}

function getItemClass(status: AnswerStatus): Record<string, boolean> {
  return {
    'answer-sheet__item--correct': status === AnswerStatus.CORRECT,
    'answer-sheet__item--incorrect': status === AnswerStatus.INCORRECT,
    'answer-sheet__item--answered': status === AnswerStatus.ANSWERED,
  }
}

function handleJump(questionId: number): void {
  emit('jump', questionId)
  emit('close')
}
</script>

<style lang="scss" scoped>
.answer-sheet {
  padding: $spacing-base $spacing-base $spacing-lg;

  &__header {
    @include flex-between;
    margin-bottom: $spacing-md;
  }

  &__title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
  }

  &__close {
    font-size: $font-size-lg;
    color: $color-text-placeholder;
    padding: $spacing-xs;
  }

  &__stats {
    @include flex-start;
    gap: $spacing-lg;
    margin-bottom: $spacing-md;
    padding-bottom: $spacing-md;
    border-bottom: 1rpx solid $color-border-light;
  }

  &__stat {
    @include flex-start;
    gap: $spacing-xs;
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }

  &__dot {
    width: 16rpx;
    height: 16rpx;
    border-radius: 50%;

    &--answered { background: $color-primary; }
    &--unanswered { background: $color-border; }
  }

  &__grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: $spacing-md;
  }

  &__item {
    aspect-ratio: 1;
    border-radius: $radius-sm;
    @include flex-center;
    background: $color-bg-gray;
    font-size: $font-size-base;
    color: $color-text-secondary;

    &--correct {
      background: $answer-correct;
      color: $color-success;
    }

    &--incorrect {
      background: $answer-wrong;
      color: $color-danger;
    }

    &--answered {
      background: $answer-selected;
      color: $color-primary;
    }
  }

  &__num {
    font-weight: $font-weight-medium;
  }
}
</style>
