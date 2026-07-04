<template>
  <div id="app">
    <!-- Noise overlay (Hermes signature) -->
    <div class="noise-overlay"></div>

    <!-- Header -->
    <header class="app-header">
      <div class="header-inner">
        <!-- Logo / Brand -->
        <router-link to="/" class="header-brand">
          <span class="brand-text">阿斯塔洛斯 Wiki</span>
        </router-link>

        <!-- Primary nav -->
        <nav class="header-nav">
          <router-link to="/" class="nav-link" exact-active-class="active">首页</router-link>

          <template v-if="userStore.isLoggedIn()">
            <router-link to="/pages" class="nav-link" active-class="active">页面</router-link>
            <router-link to="/profile" class="nav-link" active-class="active">个人</router-link>
            <router-link
              v-if="userStore.isAdmin()"
              to="/admin"
              class="nav-link"
              active-class="active"
              >管理</router-link
            >
            <a class="nav-link" @click="handleLogout" style="cursor: pointer">退出</a>
          </template>

          <template v-else>
            <router-link to="/login" class="nav-link" active-class="active">登录</router-link>
            <router-link to="/register" class="nav-link" active-class="active">注册</router-link>
          </template>
        </nav>

        <!-- Right section -->
        <div class="header-right">
          <!-- Search -->
          <div class="header-search">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索..."
              size="small"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <!-- Theme toggle -->
          <button class="theme-toggle" @click="toggleTheme" :aria-label="themeLabel">
            <span class="theme-label">{{ theme === 'dark' ? 'LIGHT' : 'DARK' }}</span>
          </button>

          <!-- Mobile menu toggle -->
          <button class="mobile-toggle" @click="showMobileMenu = !showMobileMenu" v-if="false">
            <span>☰</span>
          </button>
        </div>
      </div>
    </header>

    <!-- Sidebar (desktop) -->
    <Sidebar />

    <!-- Main content -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <Transition name="hw-page" mode="out-in">
          <component :is="Component" />
        </Transition>
      </router-view>
    </main>

    <!-- Footer -->
    <footer class="app-footer">
      <div class="footer-inner">
        <div class="footer-grid">
          <div class="footer-brand">
            <span class="footer-logo">阿斯塔洛斯 Wiki</span>
            <p class="footer-desc">奇幻世界观知识库 — 探索阿斯塔洛斯的神秘世界</p>
          </div>
          <div class="footer-links">
            <h3 class="footer-heading">导航</h3>
            <router-link to="/">首页</router-link>
            <router-link to="/search">搜索</router-link>
          </div>
          <div class="footer-links">
            <h3 class="footer-heading">功能</h3>
            <router-link to="/pages">页面中心</router-link>
            <router-link to="/create">创建页面</router-link>
          </div>
          <div class="footer-links">
            <h3 class="footer-heading">关于</h3>
            <span>Astaros Wiki</span>
            <span>© 2026</span>
          </div>
        </div>
        <div class="footer-bottom">
          <p>Astaros Wiki · 基于 Vue 3 + Spring Boot 构建</p>
          <p>MIT License · 2026</p>
        </div>
      </div>
    </footer>

    <!-- Mailbox (logged in) -->
    <Mailbox v-if="userStore.isLoggedIn()" />

    <!-- Mobile menu (unused, sidebar handles mobile) -->
    <MobileMenu :visible="showMobileMenu" @close="showMobileMenu = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from './stores/user'
import { Search } from '@element-plus/icons-vue'
import Mailbox from './components/Mailbox.vue'
import Sidebar from './components/Sidebar.vue'
import MobileMenu from './components/MobileMenu.vue'

const router = useRouter()
const userStore = useUserStore()

const searchKeyword = ref('')
const showMobileMenu = ref(false)
const theme = ref<'dark' | 'light'>('dark')
const themeLabel = ref('LIGHT')

// Initialize theme
onMounted(() => {
  const stored = localStorage.getItem('hermes-theme')
  if (stored === 'light') {
    setTheme('light')
  } else {
    setTheme('dark')
  }
})

function setTheme(t: 'dark' | 'light') {
  theme.value = t
  themeLabel.value = t === 'dark' ? 'LIGHT' : 'DARK'
  document.documentElement.setAttribute('data-theme', t)
  localStorage.setItem('hermes-theme', t)
}

function toggleTheme() {
  setTheme(theme.value === 'dark' ? 'light' : 'dark')
}

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value.trim() } })
    searchKeyword.value = ''
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/')
}
</script>

<style>
/* ---- Header ---- */
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 40;
  height: var(--header-height, 56px);
  background: color-mix(in srgb, var(--color-background) 90%, transparent);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--color-border);
}

.header-inner {
  display: flex;
  align-items: center;
  height: 100%;
  max-width: var(--max-width-container, 1400px);
  margin: 0 auto;
  padding: 0 12px;
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  height: 100%;
  border-right: 1px solid var(--color-border);
  flex-shrink: 0;
  transition: background 0.15s;
}
.header-brand:hover {
  background: color-mix(in srgb, var(--color-foreground) 5%, transparent);
}

.brand-text {
  font-family: var(--font-expanded);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--color-foreground);
}

.header-nav {
  display: flex;
  align-items: stretch;
  height: 100%;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  padding-right: 8px;
}

.header-search {
  width: 160px;
}

.header-search .el-input__wrapper {
  height: 32px !important;
  padding: 0 8px !important;
}

.theme-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  font-family: var(--font-display);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--color-muted-foreground);
  background: none;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}
.theme-toggle:hover {
  color: var(--color-foreground);
  background: color-mix(in srgb, var(--color-foreground) 10%, transparent);
}

/* ---- Main ---- */
.app-main {
  padding-top: var(--header-height, 56px);
  padding-left: 200px;
  min-height: 100vh;
}

/* ---- Footer ---- */
.app-footer {
  border-top: 1px solid var(--color-border);
  margin-top: 80px;
  padding: 48px 12px 24px;
  margin-left: 200px;
}

.footer-inner {
  max-width: var(--max-width-container);
  margin: 0 auto;
}

.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 32px;
}

.footer-brand {
  grid-column: 1;
}

.footer-logo {
  font-family: var(--font-expanded);
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--color-foreground);
}

.footer-desc {
  margin-top: 12px;
  font-size: 14px;
  color: var(--color-muted-foreground);
  line-height: 1.6;
}

.footer-heading {
  font-family: var(--font-expanded);
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.14em;
  color: var(--color-foreground);
  margin-bottom: 16px;
}

.footer-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.footer-links a,
.footer-links span {
  font-size: 14px;
  color: var(--color-muted-foreground);
  transition: color 0.15s;
}
.footer-links a:hover {
  color: var(--color-foreground);
}

.footer-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border);
  font-family: var(--font-display);
  font-size: 12px;
  color: var(--color-muted-foreground);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

/* ---- Responsive ---- */
@media (max-width: 1024px) {
  .header-nav {
    display: none;
  }
  .header-search {
    width: 120px;
  }
  .app-main {
    padding-left: 0;
  }
  .app-footer {
    margin-left: 0;
  }
  .footer-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 640px) {
  .header-brand {
    padding: 0 8px;
  }
  .brand-text {
    font-size: 13px;
  }
  .header-search {
    width: 100px;
  }
  .footer-grid {
    grid-template-columns: 1fr;
  }
  .footer-bottom {
    flex-direction: column;
    gap: 8px;
    text-align: center;
  }
}
</style>
