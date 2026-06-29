<template>
  <!-- 考试分类切换弹窗 -->
  <!-- [EXTENSION-POINT] 新增考试分类自动出现在此列表中 -->
  <van-popup
    :show="visible"
    position="bottom"
    round
    @close="handleClose"
  >
    <view class="category-switch">
      <view class="category-switch__header">
        <text class="category-switch__title">切换考试类型</text>
        <text class="category-switch__close" @tap="handleClose">✕</text>
      </view>

      <view class="category-switch__list">
        <view
          v-for="cat in categories"
          :key="cat.id"
          class="category-switch__item"
          :class="{ 'category-switch__item--active': cat.id === currentId }"
          :style="cat.id === currentId ? { borderColor: cat.color } : {}"
          @tap="handleSelect(cat.id)"
        >
          <view class="category-switch__item-icon" :style="{ background: cat.color + '1A' }">
            <text :style="{ color: cat.color, fontSize: '40rpx' }">{{ getIcon(cat.icon) }}</text>
          </view>
          <view class="category-switch__item-info">
            <text class="category-switch__item-name">{{ cat.name }}</text>
            <text class="category-switch__item-desc">{{ cat.description }}</text>
          </view>
          <view v-if="cat.id === currentId" class="category-switch__check">
            <text style="color: #4A90D9; font-size: 36rpx;">✓</text>
          </view>
        </view>
      </view>

      <!-- 未来扩展提示 -->
      <view class="category-switch__coming-soon">
        <text class="category-switch__coming-text">更多考试类型即将上线...</text>
      </view>
    </view>
  </van-popup>
</template>

<script setup lang="ts">
import { getAvailableCategories, type CategoryMeta } from '@/config/category.config'

const props = defineProps<{
  visible: boolean
  currentId: number
}>()

const emit = defineEmits<{
  close: []
  select: [id: number]
}>()

const categories = getAvailableCategories()

function getIcon(icon: string): string {
  const iconMap: Record<string, string> = {
    certificate: '📜',
    computer: '💻',
    code: '⌨️',
  }
  return iconMap[icon] || '📚'
}

function handleSelect(id: number): void {
  emit('select', id)
  emit('close')
}

function handleClose(): void {
  emit('close')
}
</script>

<style lang="scss" scoped>
.category-switch {
  padding: $spacing-base $spacing-base $spacing-lg;

  &__header {
    @include flex-between;
    margin-bottom: $spacing-md;
  }

  &__title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  &__close {
    font-size: $font-size-lg;
    color: $color-text-placeholder;
    padding: $spacing-xs;
  }

  &__list {
    @include flex-column;
    gap: $spacing-sm;
  }

  &__item {
    @include flex-start;
    padding: $spacing-md;
    background: $color-bg-page;
    border-radius: $radius-base;
    border: 2rpx solid transparent;
    transition: all 0.2s;
    gap: $spacing-md;

    &--active {
      background: $color-primary-bg;
    }
  }

  &__item-icon {
    width: 80rpx;
    height: 80rpx;
    border-radius: $radius-base;
    @include flex-center;
    flex-shrink: 0;
  }

  &__item-info {
    flex: 1;
  }

  &__item-name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    display: block;
  }

  &__item-desc {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    margin-top: 4rpx;
    display: block;
  }

  &__check {
    width: 48rpx;
    height: 48rpx;
    @include flex-center;
    flex-shrink: 0;
  }

  &__coming-soon {
    margin-top: $spacing-md;
    padding: $spacing-md;
    @include flex-center;
  }

  &__coming-text {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
  }
}
</style>
