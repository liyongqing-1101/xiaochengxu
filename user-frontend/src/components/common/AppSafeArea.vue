<template>
  <view class="app-safe-area" :style="{ paddingBottom: safeAreaBottom }">
    <slot />
  </view>
</template>

<script setup lang="ts">
const systemInfo = uni.getSystemInfoSync()
const safeAreaBottom = systemInfo.safeAreaInsets?.bottom
  ? `${systemInfo.safeAreaInsets.bottom}px`
  : 'env(safe-area-inset-bottom)'
</script>

<style lang="scss" scoped>
.app-safe-area {
  width: 100%;
  min-height: 100vh;
}
</style>
