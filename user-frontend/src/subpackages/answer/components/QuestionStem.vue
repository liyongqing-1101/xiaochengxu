<template>
  <!-- 题目题干展示组件 -->
  <view class="question-stem">
    <!-- 题型标签 -->
    <view class="question-stem__header">
      <text class="question-stem__type-tag" :style="{ color: typeMeta.color, background: typeMeta.color + '1A' }">
        {{ typeMeta.label }}
      </text>
    </view>

    <!-- 题干内容：HTML用rich-text，纯文本用text -->
    <view class="question-stem__content">
      <rich-text v-if="isHtml" :nodes="stem" />
      <text v-else class="question-stem__text">{{ stem }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { QUESTION_TYPE_CONFIG } from '@/types/question'
import type { QuestionType } from '@/types/enums'

const props = defineProps<{
  stem: string
  questionType: QuestionType
}>()

const typeMeta = computed(() => QUESTION_TYPE_CONFIG[props.questionType])

/** 判断题干是否为HTML */
const isHtml = computed(() => /<[a-zA-Z][^>]*>/.test(props.stem || ''))
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

  &__text {
    font-size: $font-size-md;
    color: $color-text-primary;
    line-height: $line-height-loose;
  }
}
</style>
