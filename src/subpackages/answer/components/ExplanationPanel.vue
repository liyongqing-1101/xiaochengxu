<template>
  <!-- 答案解析面板 -->
  <view v-if="visible" class="explanation-panel">
    <!-- 判题结果 -->
    <view class="explanation-panel__result" :class="isCorrect ? 'explanation-panel__result--correct' : 'explanation-panel__result--wrong'">
      <text class="explanation-panel__result-icon">{{ isCorrect ? '✓' : '✗' }}</text>
      <text class="explanation-panel__result-text">{{ isCorrect ? '回答正确!' : '回答错误' }}</text>
    </view>

    <!-- 正确答案(错误时显示) -->
    <view v-if="!isCorrect" class="explanation-panel__correct-answer">
      <text class="explanation-panel__label">正确答案: </text>
      <text class="explanation-panel__value">{{ correctAnswer.join(', ') }}</text>
    </view>

    <!-- 解析内容 -->
    <view class="explanation-panel__content">
      <text class="explanation-panel__label">解析</text>
      <view class="explanation-panel__text">
        <rich-text :nodes="explanation" />
      </view>
    </view>

    <!-- 知识点标签 -->
    <view v-if="tags && tags.length" class="explanation-panel__tags">
      <text
        v-for="tag in tags"
        :key="tag"
        class="explanation-panel__tag"
      >{{ tag }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean
  isCorrect: boolean
  correctAnswer: string[]
  explanation: string
  tags?: string[]
}>()
</script>

<style lang="scss" scoped>
.explanation-panel {
  margin: $spacing-md $spacing-base;
  background: $color-bg-white;
  border-radius: $radius-base;
  overflow: hidden;
  box-shadow: $shadow-sm;
}

.explanation-panel__result {
  @include flex-center;
  gap: $spacing-sm;
  padding: $spacing-md;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;

  &--correct {
    background: $answer-correct;
    color: $color-success;
  }

  &--wrong {
    background: $answer-wrong;
    color: $color-danger;
  }

  &-icon {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
  }
}

.explanation-panel__correct-answer {
  padding: $spacing-md $spacing-base;
  background: #FFF7E6;
  border-bottom: 1rpx solid $color-border-light;
}

.explanation-panel__label {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  font-weight: $font-weight-medium;
}

.explanation-panel__value {
  font-size: $font-size-base;
  color: $color-warning;
  font-weight: $font-weight-semibold;
}

.explanation-panel__content {
  padding: $spacing-md $spacing-base;
}

.explanation-panel__text {
  margin-top: $spacing-sm;
  font-size: $font-size-base;
  color: $color-text-regular;
  line-height: $line-height-loose;
}

.explanation-panel__tags {
  @include flex-start;
  flex-wrap: wrap;
  gap: $spacing-xs;
  padding: 0 $spacing-base $spacing-md;
}

.explanation-panel__tag {
  font-size: $font-size-xs;
  color: $color-primary;
  background: $color-primary-bg;
  padding: 6rpx 16rpx;
  border-radius: $radius-xl;
}
</style>
