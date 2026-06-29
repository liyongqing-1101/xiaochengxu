<template>
  <view class="app-error">
    <image
      class="app-error__image"
      src="/static/images/empty/error.png"
      mode="aspectFit"
    />
    <text class="app-error__title">{{ title }}</text>
    <text v-if="message" class="app-error__message">{{ message }}</text>
    <view v-if="retry" class="app-error__retry" @tap="handleRetry">
      <text class="app-error__retry-text">重试</text>
    </view>
  </view>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  title?: string
  message?: string
  retry?: boolean
}>(), {
  title: '加载失败',
  message: '',
  retry: true,
})

const emit = defineEmits<{
  retry: []
}>()

function handleRetry(): void {
  emit('retry')
}
</script>

<style lang="scss" scoped>
.app-error {
  @include flex-column-center;
  padding: $spacing-xxl $spacing-base;

  &__image {
    width: 200rpx;
    height: 200rpx;
    margin-bottom: $spacing-md;
    opacity: 0.6;
  }

  &__title {
    font-size: $font-size-md;
    color: $color-text-secondary;
    margin-bottom: $spacing-sm;
  }

  &__message {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
    text-align: center;
    margin-bottom: $spacing-lg;
  }

  &__retry {
    padding: $spacing-sm $spacing-lg;
    background: $color-primary;
    border-radius: $radius-xl;

    &-text {
      font-size: $font-size-base;
      color: #fff;
    }
  }
}
</style>
