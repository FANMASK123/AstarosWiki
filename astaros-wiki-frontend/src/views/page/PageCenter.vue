<template>
  <div class="page-center">
    <div class="page-center-nav">
      <div class="page-center-nav-inner">
        <router-link
          to="/pages/create"
          class="page-center-nav-item"
          :class="{ active: $route.path === '/pages/create' }"
        >
          <el-icon><Plus /></el-icon>
          <span>页面创建</span>
        </router-link>
        <router-link
          to="/pages/edit"
          class="page-center-nav-item"
          :class="{ active: $route.path === '/pages/edit' }"
        >
          <el-icon><Edit /></el-icon>
          <span>页面编辑</span>
        </router-link>
        <router-link
          to="/pages/manage"
          class="page-center-nav-item"
          :class="{ active: $route.path === '/pages/manage' }"
        >
          <el-icon><Document /></el-icon>
          <span>页面发布管理</span>
        </router-link>
      </div>
    </div>

    <div class="page-center-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { Plus, Edit, Document } from '@element-plus/icons-vue'
</script>

<style scoped>
.page-center {
  min-height: 100%;
}

.page-center-nav {
  background-color: var(--classic-bg-card);
  border-bottom: 1px solid var(--classic-border);
  position: sticky;
  top: 56px;
  z-index: 90;
}

.page-center-nav-inner {
  display: flex;
  gap: 4px;
  padding: 0 24px;
  height: 48px;
  align-items: center;
}

.page-center-nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 6px;
  color: var(--classic-text-secondary);
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
}

.page-center-nav-item:hover {
  background-color: var(--classic-bg-secondary);
  color: var(--classic-text-primary);
}

.page-center-nav-item.active {
  background-color: var(--classic-accent-primary);
  color: white;
}

.page-center-content {
  padding: 24px;
}
</style>
