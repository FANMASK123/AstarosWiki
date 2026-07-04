<template>
  <aside class="sidebar">
    <div class="sidebar-inner">
      <!-- Sidebar header -->
      <div class="sidebar-header">
        <span class="sidebar-label">功能菜单</span>
      </div>

      <!-- Menu items -->
      <div class="sidebar-menu">
        <button
          v-for="item in menuItems"
          :key="item.key"
          class="sidebar-item"
          :class="{ active: activeMenuKey === item.key }"
          @click="toggleMenu(item.key)"
        >
          <el-icon class="sidebar-item-icon">
            <component :is="item.icon" />
          </el-icon>
          <span class="sidebar-item-label">{{ item.label }}</span>
          <span class="sidebar-item-arrow" :class="{ open: activeMenuKey === item.key }">→</span>
        </button>
      </div>
    </div>

    <!-- Submenu panel -->
    <Transition name="slide">
      <div v-if="activeMenuKey" class="sidebar-panel">
        <div class="sidebar-panel-header">
          <span class="sidebar-panel-title">{{ currentMenuItem?.label }}</span>
          <button class="sidebar-panel-close" @click="closeMenu">✕</button>
        </div>
        <div class="sidebar-panel-content">
          <component :is="currentMenuItem?.component" />
        </div>
      </div>
    </Transition>
  </aside>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { FolderOpened } from '@element-plus/icons-vue'
import CategoryNav from './CategoryNav.vue'

interface MenuItem {
  key: string
  label: string
  icon: any
  component: any
}

const menuItems = ref<MenuItem[]>([
  { key: 'category', label: '分类导航', icon: FolderOpened, component: CategoryNav },
])

const activeMenuKey = ref<string | null>(null)

const currentMenuItem = computed(() =>
  menuItems.value.find((item) => item.key === activeMenuKey.value),
)

function toggleMenu(key: string) {
  activeMenuKey.value = activeMenuKey.value === key ? null : key
}

function closeMenu() {
  activeMenuKey.value = null
}
</script>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: var(--header-height, 56px);
  bottom: 0;
  width: 200px;
  z-index: 30;
}

.sidebar-inner {
  height: 100%;
  background: var(--color-card);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-label {
  font-family: var(--font-expanded);
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--color-muted-foreground);
}

.sidebar-menu {
  flex: 1;
  overflow-y: auto;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 12px 16px;
  border: none;
  border-bottom: 1px solid var(--color-border);
  background: none;
  color: var(--color-foreground);
  font-family: var(--font-display);
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s;
  text-align: left;
}
.sidebar-item:hover {
  background: color-mix(in srgb, var(--color-foreground) 8%, transparent);
}
.sidebar-item.active {
  background: color-mix(in srgb, var(--color-foreground) 12%, transparent);
}

.sidebar-item-icon {
  font-size: 14px;
  color: var(--color-muted-foreground);
  flex-shrink: 0;
}

.sidebar-item-label {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-item-arrow {
  font-size: 12px;
  color: var(--color-muted-foreground);
  transition: transform 0.2s;
  flex-shrink: 0;
}
.sidebar-item-arrow.open {
  transform: rotate(90deg);
}

/* Submenu panel */
.sidebar-panel {
  position: fixed;
  left: 200px;
  top: var(--header-height, 56px);
  bottom: 0;
  width: 280px;
  background: var(--color-card);
  border-right: 1px solid var(--color-border);
  z-index: 30;
  display: flex;
  flex-direction: column;
}

.sidebar-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-background);
}

.sidebar-panel-title {
  font-family: var(--font-expanded);
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-foreground);
}

.sidebar-panel-close {
  font-size: 14px;
  color: var(--color-muted-foreground);
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  transition: color 0.15s;
}
.sidebar-panel-close:hover {
  color: var(--color-foreground);
}

.sidebar-panel-content {
  flex: 1;
  overflow-y: auto;
}

/* Slide transition */
.slide-enter-active {
  transition: all 0.2s ease-out;
}
.slide-leave-active {
  transition: all 0.15s ease-in;
}
.slide-enter-from {
  transform: translateX(-16px);
  opacity: 0;
}
.slide-leave-to {
  transform: translateX(-16px);
  opacity: 0;
}

/* Responsive: hide sidebar on mobile */
@media (max-width: 1024px) {
  .sidebar,
  .sidebar-panel {
    display: none;
  }
}
</style>
