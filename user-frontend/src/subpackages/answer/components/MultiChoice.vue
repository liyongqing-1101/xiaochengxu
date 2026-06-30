<template>
  <!-- 多选题选项组件 -->
  <view class="multi-choice">
    <!-- 多选提示 -->
    <view v-if="!submitted" class="multi-choice__hint">
      <text class="multi-choice__hint-text">请选择所有正确答案(多选)</text>
    </view>

    <view
      v-for="option in options"
      :key="option.key"
      class="multi-choice__option"
      :class="getOptionClass(option.key)"
      @tap="handleToggle(option.key)"
    >
      <view class="multi-choice__checkbox" :class="getCheckboxClass(option.key)">
        <text v-if="selectedOptions.includes(option.key)" class="multi-choice__check-mark">✓</text>
      </view>
      <text class="multi-choice__content">{{ option.value }}</text>
      <text v-if="isCorrect(option.key)" class="multi-choice__icon">✓</text>
      <text v-if="isWrong(option.key)" class="multi-choice__icon multi-choice__icon--wrong">✗</text>
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

function getOptionClass(optionId: string): Record<string, boolean> {
  if (!props.submitted) {
    return {
      'multi-choice__option--selected': props.selectedOptions.includes(optionId),
    }
  }

  const isSelected = props.selectedOptions.includes(optionId)
  const isCorrectAnswer = props.correctAnswer.includes(optionId)

  return {
    'multi-choice__option--correct': isCorrectAnswer,
    'multi-choice__option--wrong': isSelected && !isCorrectAnswer,
  }
}

function getCheckboxClass(optionId: string): Record<string, boolean> {
  const classes: Record<string, boolean> = {
    'multi-choice__checkbox--checked': props.selectedOptions.includes(optionId),
  }

  if (props.submitted) {
    classes['multi-choice__checkbox--correct'] = props.correctAnswer.includes(optionId)
    classes['multi-choice__checkbox--wrong'] =
      props.selectedOptions.includes(optionId) && !props.correctAnswer.includes(optionId)
  }

  return classes
}

function isCorrect(optionId: string): boolean {
  return props.submitted && props.correctAnswer.includes(optionId)
}

function isWrong(optionId: string): boolean {
  return (
    props.submitted &&
    props.selectedOptions.includes(optionId) &&
    !props.correctAnswer.includes(optionId)
  )
}

function handleToggle(optionId: string): void {
  if (props.submitted) return
  emit('select', optionId)
}
</script>

<style lang="scss" scoped>
.multi-choice {
  padding: 0 $spacing-base;
  @include flex-column;
  gap: $spacing-sm;
}

.multi-choice__hint {
  padding: $spacing-sm $spacing-md;
  background: #FFF7E6;
  border-radius: $radius-sm;
  margin-bottom: $spacing-xs;

  &-text {
    font-size: $font-size-xs;
    color: $color-warning;
  }
}

.multi-choice__option {
  @include flex-start;
  padding: $spacing-md;
  background: $color-bg-page;
  border-radius: $radius-base;
  border: 2rpx solid transparent;
  gap: $spacing-md;
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

.multi-choice__checkbox {
  width: 44rpx;
  height: 44rpx;
  border-radius: $radius-sm;
  border: 2rpx solid $color-border-dark;
  @include flex-center;
  flex-shrink: 0;
  transition: all 0.2s;

  &--checked {
    background: $color-primary;
    border-color: $color-primary;
  }

  &--correct {
    background: $color-success;
    border-color: $color-success;
  }

  &--wrong {
    background: $color-danger;
    border-color: $color-danger;
  }
}

.multi-choice__check-mark {
  font-size: 24rpx;
  color: #fff;
  font-weight: $font-weight-bold;
}

.multi-choice__content {
  font-size: $font-size-base;
  color: $color-text-regular;
  flex: 1;
  line-height: $line-height-base;
}

.multi-choice__icon {
  font-size: $font-size-md;
  color: $color-success;
  font-weight: $font-weight-bold;
  flex-shrink: 0;

  &--wrong {
    color: $color-danger;
  }
}
</style>
