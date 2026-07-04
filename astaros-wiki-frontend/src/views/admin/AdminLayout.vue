<template>
  <div class="admin-layout">
    <div class="admin-sidebar">
      <el-menu :default-active="activeMenu" router>
        <el-menu-item index="/admin">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tags">
          <el-icon><PriceTag /></el-icon>
          <span>标签管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/pages">
          <el-icon><Document /></el-icon>
          <span>页面管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/logs">
          <el-icon><Tickets /></el-icon>
          <span>操作日志</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="admin-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Odometer, User, Folder, PriceTag, Document, Tickets } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 80vh;
}

.admin-sidebar {
  width: 200px;
  background-color: var(--classic-bg-card);
  border-right: 1px solid var(--classic-border);
}

.admin-content {
  flex: 1;
  padding: 24px;
}
</style>
