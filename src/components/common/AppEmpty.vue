<template>
  <view class="app-empty">
    <image
      class="app-empty__image"
      :src="image || defaultImage"
      mode="aspectFit"
    />
    <text class="app-empty__title">{{ title }}</text>
    <text v-if="description" class="app-empty__desc">{{ description }}</text>
    <view v-if="$slots.default" class="app-empty__action">
      <slot />
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  title?: string
  description?: string
  image?: string
}>(), {
  title: '暂无内容',
  description: '',
})

const defaultImage = '/static/images/empty/default.png'
</script>

<style lang="scss" scoped>
.app-empty {
  @include flex-column-center;
  padding: $spacing-xxl $spacing-base;

  &__image {
    width: 240rpx;
    height: 240rpx;
    margin-bottom: $spacing-md;
    opacity: 0.6;
  }

  &__title {
    font-size: $font-size-md;
    color: $color-text-secondary;
    margin-bottom: $spacing-sm;
  }

  &__desc {
    font-size: $font-size-sm;
    color: $color-text-placeholder;
    text-align: center;
    line-height: $line-height-loose;
  }

  &__action {
    margin-top: $spacing-lg;
  }
}
</style>
