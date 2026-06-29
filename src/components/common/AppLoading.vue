<template>
  <view v-if="visible" class="app-loading">
    <view class="app-loading__mask" />
    <view class="app-loading__content">
      <view class="app-loading__spinner" />
      <text class="app-loading__text">{{ text }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean
  text?: string
}>()
</script>

<style lang="scss" scoped>
.app-loading {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: $z-index-toast;
  @include flex-center;

  &__mask {
    @include absolute-fill;
    background: rgba(255, 255, 255, 0.8);
  }

  &__content {
    position: relative;
    z-index: 1;
    @include flex-column-center;
    gap: $spacing-md;
  }

  &__spinner {
    width: 64rpx;
    height: 64rpx;
    border: 4rpx solid $color-border;
    border-top-color: $color-primary;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  &__text {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
