<template>
  <!-- 单选题选项组件 -->
  <view class="single-choice">
    <view
      v-for="option in options"
      :key="option.key"
      class="single-choice__option"
      :class="getOptionClass(option.key)"
      @tap="handleSelect(option.key)"
    >
      <view class="single-choice__radio" :class="getRadioClass(option.key)">
        <text class="single-choice__radio-label">{{ option.key }}</text>
      </view>
      <text class="single-choice__content">{{ option.value }}</text>
      <text v-if="isCorrect(option.key)" class="single-choice__icon">✓</text>
      <text v-if="isWrong(option.key)" class="single-choice__icon single-choice__icon--wrong">✗</text>
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

function getOptionClass(optionKey: string): Record<string, boolean> {
  if (!props.submitted) {
    return {
      'single-choice__option--selected': props.selectedOptions.includes(optionKey),
    }
  }

  const isSelected = props.selectedOptions.includes(optionKey)
  const isCorrectAnswer = props.correctAnswer.includes(optionKey)

  return {
    'single-choice__option--correct': isCorrectAnswer,
    'single-choice__option--wrong': isSelected && !isCorrectAnswer,
    'single-choice__option--selected-wrong': isSelected && !isCorrectAnswer,
  }
}

function getRadioClass(optionKey: string): Record<string, boolean> {
  return {
    'single-choice__radio--selected': props.selectedOptions.includes(optionKey),
    'single-choice__radio--correct': props.submitted && props.correctAnswer.includes(optionKey),
    'single-choice__radio--wrong':
      props.submitted &&
      props.selectedOptions.includes(optionKey) &&
      !props.correctAnswer.includes(optionKey),
  }
}

function isCorrect(optionKey: string): boolean {
  return props.submitted && props.correctAnswer.includes(optionKey)
}

function isWrong(optionKey: string): boolean {
  return (
    props.submitted &&
    props.selectedOptions.includes(optionKey) &&
    !props.correctAnswer.includes(optionKey)
  )
}

function handleSelect(optionKey: string): void {
  if (props.submitted) return
  emit('select', optionKey)
}
</script>

<style lang="scss" scoped>
.single-choice {
  padding: 0 $spacing-base;
  @include flex-column;
  gap: $spacing-sm;
}

.single-choice__option {
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

.single-choice__radio {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  border: 2rpx solid $color-border-dark;
  @include flex-center;
  flex-shrink: 0;
  transition: all 0.2s;

  &--selected {
    background: $color-primary;
    border-color: $color-primary;
    .single-choice__radio-label {
      color: #fff;
    }
  }

  &--correct {
    background: $color-success;
    border-color: $color-success;
    .single-choice__radio-label {
      color: #fff;
    }
  }

  &--wrong {
    background: $color-danger;
    border-color: $color-danger;
    .single-choice__radio-label {
      color: #fff;
    }
  }
}

.single-choice__radio-label {
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: $color-text-secondary;
}

.single-choice__content {
  font-size: $font-size-base;
  color: $color-text-regular;
  flex: 1;
  line-height: $line-height-base;
}

.single-choice__icon {
  font-size: $font-size-md;
  color: $color-success;
  font-weight: $font-weight-bold;
  flex-shrink: 0;

  &--wrong {
    color: $color-danger;
  }
}
</style>
