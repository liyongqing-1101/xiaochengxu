<template>
  <!-- 判断题选项组件 -->
  <view class="true-false">
    <view
      v-for="option in options"
      :key="option.key"
      class="true-false__btn"
      :class="getBtnClass(option.key)"
      @tap="handleSelect(option.key)"
    >
      <text class="true-false__emoji">{{ option.key === 'true' ? '✓' : '✗' }}</text>
      <text class="true-false__label">{{ option.value }}</text>
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

function getBtnClass(optionKey: string): Record<string, boolean> {
  if (!props.submitted) {
    return {
      'true-false__btn--selected': props.selectedOptions.includes(optionKey),
    }
  }

  const isSelected = props.selectedOptions.includes(optionKey)
  const isCorrectAnswer = props.correctAnswer.includes(optionKey)

  return {
    'true-false__btn--correct': isCorrectAnswer,
    'true-false__btn--wrong': isSelected && !isCorrectAnswer,
  }
}

function handleSelect(optionKey: string): void {
  if (props.submitted) return
  emit('select', optionKey)
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
