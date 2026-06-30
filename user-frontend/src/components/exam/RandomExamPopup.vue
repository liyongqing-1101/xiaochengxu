<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    round
    close-on-click-overlay
    custom-style="border-radius: 24rpx 24rpx 0 0;"
    @close="$emit('close')"
  >
    <view class="random-popup">
      <!-- 标题行 -->
      <view class="random-popup__header">
        <text class="random-popup__title">随机刷题</text>
        <view class="random-popup__close" @tap.stop="$emit('close')">
          <text class="random-popup__close-icon">✕</text>
        </view>
      </view>

      <!-- 副标题 -->
      <text class="random-popup__subtitle">选择科目</text>

      <!-- 科目标签（横向4个并排） -->
      <view class="random-popup__tags">
        <view
          v-for="subject in subjects"
          :key="subject.id"
          class="random-popup__tag"
          :class="{ 'random-popup__tag--active': selectedId === subject.id }"
          @tap="handleSelect(subject.id)"
        >
          <text>{{ subject.name }}</text>
        </view>
      </view>

      <!-- 提示文字 -->
      <text class="random-popup__hint">不选则刷全部科目</text>

      <!-- 开始按钮 -->
      <view class="random-popup__btn" @tap="handleStart">
        <text class="random-popup__btn-text">开始刷题</text>
      </view>

      <!-- 底部安全区 -->
      <view class="random-popup__safe-bottom" />
    </view>
  </van-popup>
</template>

<script setup lang="ts">
import type { Subject } from '@/types/exam'

interface Props {
  visible: boolean
  subjects: Subject[]
  selectedId: number | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'select', id: number | null): void
  (e: 'start', subjectId: number | null): void
}>()

function handleSelect(id: number): void {
  // 单选逻辑：点击已选中则取消
  const newId = props.selectedId === id ? null : id
  emit('select', newId)
}

function handleStart(): void {
  emit('start', props.selectedId)
}
</script>

<style lang="scss" scoped>
.random-popup {
  padding: 48rpx 32rpx 32rpx;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 32rpx;
  }

  &__title {
    font-size: 40rpx;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
  }

  &__close {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: -16rpx; // 扩大点击热区
  }

  &__close-icon {
    font-size: 36rpx;
    color: $color-text-secondary;
  }

  &__subtitle {
    font-size: 28rpx;
    color: $color-text-secondary;
    margin-bottom: 24rpx;
    display: block;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    margin-bottom: 24rpx;
  }

  &__tag {
    padding: 16rpx 32rpx;
    background: #F5F7FA;
    border-radius: 40rpx;
    transition: all 0.2s;

    text {
      font-size: 28rpx;
      color: $color-text-secondary;
    }

    &--active {
      background: $color-primary;

      text {
        color: #fff;
        font-weight: $font-weight-medium;
      }
    }
  }

  &__hint {
    font-size: 24rpx;
    color: $color-text-placeholder;
    margin-bottom: 48rpx;
    display: block;
  }

  &__btn {
    height: 96rpx;
    background: #1D2129;
    border-radius: 24rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    &-text {
      font-size: 32rpx;
      font-weight: $font-weight-semibold;
      color: #fff;
    }
  }

  &__safe-bottom {
    height: env(safe-area-inset-bottom);
  }
}
</style>
