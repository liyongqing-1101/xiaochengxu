<template>
  <!-- 全局搜索栏 -->
  <view class="search-bar" @tap="handleFocus">
    <view class="search-bar__inner">
      <image class="search-bar__icon" src="/static/images/icons/search.png" mode="aspectFit" />
      <text class="search-bar__placeholder">{{ placeholder }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  placeholder?: string
}>(), {
  placeholder: '搜索题目、知识点...',
})

const emit = defineEmits<{
  click: []
}>()

function handleFocus(): void {
  emit('click')
  // 跳转到学习页搜索模式
  uni.navigateTo({
    url: '/pages/study/index?focusSearch=1',
  })
}
</script>

<style lang="scss" scoped>
.search-bar {
  margin: $spacing-md $spacing-base;

  &__inner {
    @include flex-start;
    height: 72rpx;
    background: $color-bg-gray;
    border-radius: 36rpx;
    padding: 0 $spacing-md;
    gap: $spacing-sm;
  }

  &__icon {
    width: 36rpx;
    height: 36rpx;
    opacity: 0.4;
    flex-shrink: 0;
  }

  &__placeholder {
    font-size: $font-size-base;
    color: $color-text-placeholder;
  }
}
</style>
