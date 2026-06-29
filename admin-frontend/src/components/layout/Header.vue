<template>
  <div class="header-content flex-between" style="width:100%">
    <div></div>
    <div class="header-right">
      <span class="nickname">{{ auth.nickname }}</span>
      <el-button type="danger" text @click="auth.logout()">退出登录</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>

<style scoped lang="scss">
.header-content {
  width: 100%;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.nickname {
  color: #606266;
  font-size: 14px;
}
</style>
