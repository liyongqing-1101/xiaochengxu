<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="admin-aside">
      <Sidebar />
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <Header />
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
</script>

<style scoped lang="scss">
.admin-layout {
  height: 100vh;
}
.admin-aside {
  background: #304156;
  overflow-y: auto;
}
.admin-header {
  background: #fff;
  border-bottom: 1px solid #EBEEF5;
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 56px;
}
.admin-main {
  background: #F5F7FA;
  min-height: calc(100vh - 56px);
  padding: 16px;
}
</style>
