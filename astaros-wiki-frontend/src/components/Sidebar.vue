<template>
  <div class="sidebar-shell">
    <!-- Sidebar menu bar -->
    <div class="sidebar-bar">
      <div class="sidebar-hd"><span class="eyebrow">功能菜单</span></div>
      <div class="sidebar-items">
        <button
          v-for="item in menuItems"
          :key="item.id"
          class="sidebar-item"
          :class="{ active: activeMenu === item.id }"
          @click="toggleMenu(item.id)"
        >
          <span class="sidebar-item-icon">{{ item.icon }}</span>
          <span class="sidebar-item-label">{{ item.label }}</span>
          <span class="sidebar-item-arrow" :class="{ open: activeMenu === item.id }">▸</span>
        </button>
      </div>
    </div>

    <!-- Slide-out panel (level 1) -->
    <Transition name="slide">
      <div v-if="activeMenu && currentMenuItem" class="sidebar-panel panel-l1">
        <div class="panel-hd">
          <span class="panel-title">{{ currentMenuItem.label }}</span>
          <button class="panel-close" @click="closeAll">✕</button>
        </div>
        <div class="panel-bd">
          <component :is="currentMenuItem.component" @navigate="onNavigate" />
        </div>
      </div>
    </Transition>

    <!-- Slide-out sub-panel (level 2) — managed by CategoryNav -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import CategoryNav from './CategoryNav.vue'

interface MenuItem {
  id: string
  label: string
  icon: string
  component: any
}

const menuItems: MenuItem[] = [
  { id: 'category', label: '分类导航', icon: '📁', component: CategoryNav },
  // 未来可在此添加更多菜单项:
  // { id: 'tags', label: '标签云', icon: '🏷️', component: TagCloud },
  // { id: 'recent', label: '最近浏览', icon: '🕐', component: RecentPages },
]

const activeMenu = ref<string | null>(null)

const currentMenuItem = computed(() =>
  menuItems.find((m) => m.id === activeMenu.value)
)

function toggleMenu(id: string) {
  activeMenu.value = activeMenu.value === id ? null : id
}

function closeAll() {
  activeMenu.value = null
}

// CategoryNav emits 'navigate' → close panels
function onNavigate() {
  // keep panel open, just navigated within
}
</script>

<style scoped>
/* ── Shell ───────────────────────── */
.sidebar-shell {
  position: relative;
  z-index: 30;
}

/* ── Bar ─────────────────────────── */
.sidebar-bar {
  width: 170px;
  flex-shrink: 0;
  background: var(--card);
  border-right: 1px solid var(--border);
  min-height: 100%;
}

.sidebar-hd {
  padding: 10px 14px;
  border-bottom: 1px solid var(--border);
}

.sidebar-items {
  padding: 4px 0;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 10px 14px;
  background: none;
  border: none;
  border-bottom: 1px solid var(--border);
  color: var(--fg);
  font: inherit;
  font-size: 13px;
  cursor: pointer;
  text-align: left;
  transition: background 0.15s;
}
.sidebar-item:hover {
  background: color-mix(in srgb, var(--fg) 6%, transparent);
}
.sidebar-item.active {
  background: color-mix(in srgb, var(--fg) 10%, transparent);
}

.sidebar-item-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.sidebar-item-label {
  flex: 1;
  white-space: nowrap;
}

.sidebar-item-arrow {
  font-size: 10px;
  color: var(--muted);
  transition: transform 0.2s;
  flex-shrink: 0;
}
.sidebar-item-arrow.open {
  transform: rotate(90deg);
}

/* ── Panel ───────────────────────── */
.sidebar-panel {
  position: fixed;
  top: 56px;
  bottom: 0;
  background: var(--card);
  border-right: 1px solid var(--border);
  z-index: 29;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.panel-l1 {
  left: 170px;
  width: 240px;
}

.panel-hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-bottom: 1px solid var(--border);
  background: var(--bg);
  flex-shrink: 0;
}

.panel-title {
  font-weight: 700;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.panel-close {
  background: none;
  border: none;
  color: var(--muted);
  cursor: pointer;
  font-size: 14px;
  padding: 2px 4px;
  transition: color 0.15s;
}
.panel-close:hover {
  color: var(--fg);
}

.panel-bd {
  flex: 1;
  overflow-y: auto;
}

/* ── Transition ──────────────────── */
.slide-enter-active {
  transition: transform 0.2s ease-out, opacity 0.2s ease-out;
}
.slide-leave-active {
  transition: transform 0.15s ease-in, opacity 0.15s ease-in;
}
.slide-enter-from {
  transform: translateX(-16px);
  opacity: 0;
}
.slide-leave-to {
  transform: translateX(-16px);
  opacity: 0;
}

/* ── Responsive ──────────────────── */
@media (max-width: 1024px) {
  .sidebar-bar,
  .sidebar-panel {
    display: none;
  }
}
</style>
