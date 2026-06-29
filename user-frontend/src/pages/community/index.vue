<template>
  <!-- 社区 — 占位页面 -->
  <!-- [EXTENSION-POINT] 后续扩展社区发帖、讨论功能 -->
  <view class="placeholder-page">
    <AppEmpty
      title="社区"
      description="学习社区功能即将上线，敬请期待"
    />
    <CustomTabbar
      current-path="/pages/community/index"
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
