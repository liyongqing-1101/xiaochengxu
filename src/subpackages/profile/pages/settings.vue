<template>
  <!-- 设置页面 -->
  <view class="settings-page">
    <van-cell-group inset>
      <van-cell title="清除缓存" is-link @click="handleClearCache" />
      <van-cell title="隐私政策" is-link @click="handlePrivacy" />
      <van-cell title="用户协议" is-link @click="handleAgreement" />
      <van-cell title="关于我们" is-link :value="version" @click="handleAbout" />
    </van-cell-group>
  </view>
</template>

<script setup lang="ts">
import { APP } from '@/utils/constants'

const version = `v${APP.VERSION}`

async function handleClearCache(): Promise<void> {
  uni.showModal({
    title: '清除缓存',
    content: '将清除本地搜索历史和缓存数据',
    success: async (res) => {
      if (res.confirm) {
        // 保留 token 和用户信息，清除其他缓存
        const { storage } = await import('@/utils/storage')
        storage.remove('app:search_history')
        storage.remove('app:question_cache')
        uni.showToast({ title: '已清除', icon: 'success' })
      }
    },
  })
}

function handlePrivacy(): void {
  uni.showToast({ title: '隐私政策页面', icon: 'none' })
}

function handleAgreement(): void {
  uni.showToast({ title: '用户协议页面', icon: 'none' })
}

function handleAbout(): void {
  uni.showModal({
    title: '关于我们',
    content: `${APP.NAME} v${APP.VERSION}\n专注高校教资考试刷题`,
    showCancel: false,
  })
}
</script>

<style lang="scss" scoped>
.settings-page {
  min-height: 100vh;
  background: $color-bg-page;
  padding-top: $spacing-sm;
}
</style>
