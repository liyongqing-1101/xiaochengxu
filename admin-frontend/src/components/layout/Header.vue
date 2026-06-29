<template>
  <div class="header">
    <div class="header-left">
      <h2 class="header-title">高校教资刷题 · 管理后台</h2>
    </div>
    <div class="header-right">
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="user-info">
          <span class="avatar">👤</span>
          <span class="nickname">{{ authStore.nickname || '管理员' }}</span>
          <span class="arrow">▾</span>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item disabled>
              <span class="dropdown-role">{{ roleLabel }}</span>
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <span style="color: #f56c6c;">退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const roleLabel = computed(() => {
  const map: Record<string, string> = {
    SUPER_ADMIN: '超级管理员',
    ADMIN: '管理员',
  }
  return map[authStore.role] || authStore.role || '管理员'
})

function handleCommand(command: string) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning',
      confirmButtonText: '退出',
      cancelButtonText: '取消',
    }).then(() => {
      authStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 6px;
  transition: background 0.2s;
  user-select: none;
}

.user-info:hover {
  background: #f5f7fa;
}

.avatar {
  font-size: 20px;
}

.nickname {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.arrow {
  font-size: 12px;
  color: #909399;
}

.dropdown-role {
  color: #909399;
  font-size: 12px;
}
</style>