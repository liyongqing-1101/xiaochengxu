<template>
  <!-- 应用根组件 -->
  <view class="app-root">
    <!-- 页面路由出口 -->
    <router-view />
  </view>
</template>

<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useExamStore } from '@/stores/exam'
import { useWrongBookStore } from '@/stores/wrongBook'
import { registerInterceptors } from '@/api/interceptors'

/**
 * 应用启动生命周期
 */
onLaunch(() => {
  console.log('[App] onLaunch')

  // 1. 注册请求拦截器(注入 token、401 跳转)
  registerInterceptors()

  // 2. 恢复登录会话
  const userStore = useUserStore()
  userStore.restoreSession()

  // 3. 恢复错题本
  const wrongBookStore = useWrongBookStore()
  wrongBookStore.loadFromStorage()

  // 4. 预加载考试分类数据
  const examStore = useExamStore()
  examStore.fetchCategories().catch(() => {
    // 静默失败, 页面会显示空状态
  })
})

onShow(() => {
  console.log('[App] onShow')
  // 可以在此刷新 token 或检查更新
})

onHide(() => {
  console.log('[App] onHide')
  // 可以在此保存临时数据
})
</script>

<style lang="scss">
.app-root {
  width: 100%;
  min-height: 100vh;
}
</style>
