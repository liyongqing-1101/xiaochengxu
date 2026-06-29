<template>
  <!-- 判断题选项组件 -->
  <view class="true-false">
    <view
      v-for="option in options"
      :key="option.id"
      class="true-false__btn"
      :class="getBtnClass(option.id)"
      @tap="handleSelect(option.id)"
    >
      <text class="true-false__emoji">{{ option.id === 'true' || option.content.includes('正确') ? '✓' : '✗' }}</text>
      <text class="true-false__label">{{ option.content }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { QuestionOption } from '@/types/question'

const props = defineProps<{
  options: QuestionOption[]
  selectedOptions: string[]
  correctAnswer: string[]
  submitted: boolean
}>()

const emit = defineEmits<{
  select: [optionId: string]
}>()

function getBtnClass(optionId: string): Record<string, boolean> {
  if (!props.submitted) {
    return {
      'true-false__btn--selected': props.selectedOptions.includes(optionId),
    }
  }

  const isSelected = props.selectedOptions.includes(optionId)
  const isCorrectAnswer = props.correctAnswer.includes(optionId)

  return {
    'true-false__btn--correct': isCorrectAnswer,
    'true-false__btn--wrong': isSelected && !isCorrectAnswer,
  }
}

function handleSelect(optionId: string): void {
  if (props.submitted) return
  emit('select', optionId)
}
</script>

<style lang="scss" scoped>
.true-false {
  padding: 0 $spacing-base;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: $spacing-md;
}

.true-false__btn {
  @include flex-column-center;
  padding: $spacing-xl $spacing-md;
  background: $color-bg-page;
  border-radius: $radius-base;
  border: 2rpx solid transparent;
  gap: $spacing-sm;
  transition: all 0.2s;

  &--selected {
    background: $answer-selected;
    border-color: $answer-selected-border;
  }

  &--correct {
    background: $answer-correct;
    border-color: $answer-correct-border;
  }

  &--wrong {
    background: $answer-wrong;
    border-color: $answer-wrong-border;
  }
}

.true-false__emoji {
  font-size: 64rpx;
  font-weight: $font-weight-bold;
}

.true-false__label {
  font-size: $font-size-md;
  color: $color-text-regular;
  font-weight: $font-weight-medium;
}
</style>
