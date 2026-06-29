<template>
  <!-- 教辅 — 占位页面 -->
  <view class="placeholder-page">
    <AppEmpty
      title="教辅资料"
      description="教辅资料功能即将上线，敬请期待"
    />
    <CustomTabbar
      current-path="/pages/teaching-aid/index"
      @change="handleTabChange"
    />
  </view>
</template>

<script setup lang="ts">
import AppEmpty from '@components/common/AppEmpty.vue'
import CustomTabbar from '@components/tabbar/CustomTabbar.vue'

function handleTabChange(path: string): void {
  uni.switchTab({ url: path })
}
</script>

<style lang="scss" scoped>
.placeholder-page {
  min-height: 100vh;
  background: $color-bg-page;
  @include flex-center;
}
</style>
