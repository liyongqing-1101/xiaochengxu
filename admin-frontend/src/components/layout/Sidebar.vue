<template>
  <el-menu
    router
    :default-active="route.path"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#4A90D9"
  >
    <div class="logo-area">
      <h3>高校教资题库</h3>
      <p>管理后台</p>
    </div>
    <el-menu-item index="/question">
      <el-icon><Document /></el-icon>
      <span>题库管理</span>
    </el-menu-item>
    <el-menu-item index="/category">
      <el-icon><FolderOpened /></el-icon>
      <span>分类管理</span>
    </el-menu-item>
    <el-menu-item index="/import">
      <el-icon><Upload /></el-icon>
      <span>导入记录</span>
    </el-menu-item>
  </el-menu>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { Document, FolderOpened, Upload } from '@element-plus/icons-vue'

const route = useRoute()
</script>

<style scoped lang="scss">
.logo-area {
  padding: 20px 16px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 8px;
  h3 {
    color: #fff;
    font-size: 16px;
    margin-bottom: 4px;
  }
  p {
    color: #8c98a8;
    font-size: 12px;
  }
}
.el-menu {
  border-right: none;
}
</style>
