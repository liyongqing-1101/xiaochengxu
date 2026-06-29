<template>
  <!-- 题目卡片组件 — 纯展示 -->
  <view class="question-card" :class="{ 'question-card--active': active }">
    <!-- 头部: 题型标签 + 难度标签 + 科目 -->
    <view class="question-card__header">
      <view class="question-card__tags">
        <text class="question-card__type-tag" :style="{ color: typeMeta.color, background: typeMeta.color + '1A' }">
          {{ typeMeta.label }}
        </text>
        <text class="question-card__difficulty-tag" :style="{ color: diffColor }">
          {{ diffLabel }}
        </text>
      </view>
      <text v-if="showSubject" class="question-card__subject">{{ question.subjectName }}</text>
    </view>

    <!-- 题干 -->
    <view class="question-card__stem">
      <rich-text :nodes="question.stem" />
    </view>

    <!-- 选项列表(预览模式, 不显示交互) -->
    <view v-if="showOptions" class="question-card__options">
      <view
        v-for="opt in question.options"
        :key="opt.id"
        class="question-card__option"
      >
        <text class="question-card__option-label">{{ opt.label }}.</text>
        <text class="question-card__option-content">{{ opt.content }}</text>
      </view>
    </view>

    <!-- 底部: 知识点标签 + 操作区 -->
    <view v-if="showFooter" class="question-card__footer">
      <view class="question-card__knowledge-tags">
        <text
          v-for="tag in question.tags.slice(0, 3)"
          :key="tag"
          class="question-card__tag"
        >{{ tag }}</text>
      </view>
    </view>

    <!-- 错误信息(错题本模式) -->
    <view v-if="showErrorInfo" class="question-card__error-info">
      <text class="question-card__error-count">做错 {{ errorCount }} 次</text>
      <text class="question-card__error-answer">
        你的答案: {{ lastWrongAnswer }}
      </text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Question } from '@/types/question'
import { QUESTION_TYPE_CONFIG, DIFFICULTY_CONFIG } from '@/types/question'
import type { Difficulty } from '@/types/enums'

const props = withDefaults(defineProps<{
  question: Question
  active?: boolean
  showSubject?: boolean
  showOptions?: boolean
  showFooter?: boolean
  /** 错题本模式 */
  showErrorInfo?: boolean
  errorCount?: number
  lastWrongAnswer?: string
}>(), {
  active: false,
  showSubject: true,
  showOptions: false,
  showFooter: true,
  showErrorInfo: false,
  errorCount: 0,
  lastWrongAnswer: '',
})

const typeMeta = computed(() => QUESTION_TYPE_CONFIG[props.question.type])
const diffColor = computed(() => DIFFICULTY_CONFIG[props.question.difficulty as Difficulty]?.color || '#999')
const diffLabel = computed(() => DIFFICULTY_CONFIG[props.question.difficulty as Difficulty]?.label || '')
</script>

<style lang="scss" scoped>
.question-card {
  @include card;
  margin: 0 $spacing-base $spacing-md;
  transition: all 0.2s;

  &--active {
    border: 2rpx solid $color-primary;
    box-shadow: 0 0 0 4rpx $color-primary-bg;
  }

  &__header {
    @include flex-between;
    margin-bottom: $spacing-sm;
  }

  &__tags {
    @include flex-start;
    gap: $spacing-xs;
  }

  &__type-tag {
    font-size: $font-size-xs;
    padding: 4rpx 12rpx;
    border-radius: $radius-sm;
  }

  &__difficulty-tag {
    font-size: $font-size-xs;
  }

  &__subject {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }

  &__stem {
    font-size: $font-size-base;
    color: $color-text-regular;
    line-height: $line-height-loose;
    margin-bottom: $spacing-sm;
  }

  &__options {
    margin-top: $spacing-sm;
  }

  &__option {
    padding: $spacing-xs 0;
    font-size: $font-size-sm;
    color: $color-text-secondary;
    line-height: $line-height-base;

    &-label {
      font-weight: $font-weight-medium;
      margin-right: $spacing-xs;
      color: $color-text-regular;
    }
  }

  &__footer {
    margin-top: $spacing-sm;
  }

  &__knowledge-tags {
    @include flex-start;
    flex-wrap: wrap;
    gap: $spacing-xs;
  }

  &__tag {
    font-size: $font-size-xs;
    color: $color-primary;
    background: $color-primary-bg;
    padding: 4rpx 16rpx;
    border-radius: $radius-xl;
  }

  &__error-info {
    margin-top: $spacing-sm;
    padding-top: $spacing-sm;
    border-top: 1rpx solid $color-border-light;
    @include flex-column;
    gap: $spacing-xs;
  }

  &__error-count {
    font-size: $font-size-sm;
    color: $color-danger;
  }

  &__error-answer {
    font-size: $font-size-xs;
    color: $color-text-placeholder;
  }
}
</style>
