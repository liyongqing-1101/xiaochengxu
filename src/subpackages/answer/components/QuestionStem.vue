<template>
  <!-- 题目题干展示组件 -->
  <view class="question-stem">
    <!-- 题型 + 难度标签 -->
    <view class="question-stem__header">
      <text class="question-stem__type-tag" :style="{ color: typeMeta.color, background: typeMeta.color + '1A' }">
        {{ typeMeta.label }}
      </text>
      <text class="question-stem__difficulty" :style="{ color: diffColor }">
        {{ diffLabel }}
      </text>
    </view>

    <!-- 题干内容 -->
    <view class="question-stem__content">
      <rich-text :nodes="stem" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { QUESTION_TYPE_CONFIG, DIFFICULTY_CONFIG } from '@/types/question'
import type { QuestionType, Difficulty } from '@/types/enums'

const props = defineProps<{
  stem: string
  questionType: QuestionType
  difficulty: Difficulty
}>()

const typeMeta = computed(() => QUESTION_TYPE_CONFIG[props.questionType])
const diffColor = computed(() => DIFFICULTY_CONFIG[props.difficulty]?.color || '#999')
const diffLabel = computed(() => DIFFICULTY_CONFIG[props.difficulty]?.label || '')
</script>

<style lang="scss" scoped>
.question-stem {
  padding: $spacing-md $spacing-base;

  &__header {
    @include flex-start;
    gap: $spacing-sm;
    margin-bottom: $spacing-md;
  }

  &__type-tag {
    font-size: $font-size-xs;
    padding: 6rpx 16rpx;
    border-radius: $radius-sm;
    font-weight: $font-weight-medium;
  }

  &__difficulty {
    font-size: $font-size-xs;
  }

  &__content {
    font-size: $font-size-md;
    color: $color-text-primary;
    line-height: $line-height-loose;
    letter-spacing: 0.5px;
  }
}
</style>
