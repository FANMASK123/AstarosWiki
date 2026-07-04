<template>
  <div id="app" class="app-shell">
    <div class="noise-overlay"></div>

    <!-- Header -->
    <header class="app-header">
      <router-link to="/" class="app-header-brand">阿斯塔洛斯 Wiki</router-link>
      <nav class="app-header-nav">
        <router-link to="/" class="nav-link" exact-active-class="active">首页</router-link>
        <template v-if="userStore.isLoggedIn()">
          <router-link to="/pages" class="nav-link" active-class="active">页面</router-link>
          <router-link to="/profile" class="nav-link" active-class="active">个人</router-link>
          <router-link v-if="userStore.isAdmin()" to="/admin" class="nav-link" active-class="active">管理</router-link>
          <a class="nav-link" style="cursor:pointer" @click="handleLogout">退出</a>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-link" active-class="active">登录</router-link>
          <router-link to="/register" class="nav-link" active-class="active">注册</router-link>
        </template>
      </nav>
      <div class="app-header-right">
        <el-input v-model="searchKeyword" placeholder="搜索..." size="small" style="width:150px" @keyup.enter="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <button class="btn btn-ghost" style="padding:5px 12px;font-size:10px" @click="toggleTheme">
          {{ theme === 'dark' ? 'LIGHT' : 'DARK' }}
        </button>
      </div>
    </header>

    <!-- Main -->
    <div class="app-main">
      <aside class="app-sidebar">
        <Sidebar />
      </aside>
      <main class="app-content">
        <router-view v-slot="{ Component }">
          <Transition name="page" mode="out-in">
            <component :is="Component" />
          </Transition>
        </router-view>
      </main>
    </div>

    <!-- Footer -->
    <footer class="app-footer">
      <div class="app-footer-grid">
        <div>
          <span style="font-weight:700;font-size:16px;text-transform:uppercase;letter-spacing:.06em">阿斯塔洛斯 Wiki</span>
          <p class="text-muted mt-16">奇幻世界观知识库 — 探索阿斯塔洛斯的神秘世界</p>
        </div>
        <div>
          <div class="footer-heading">导航</div>
          <div class="footer-links">
            <router-link to="/">首页</router-link>
            <router-link to="/search">搜索</router-link>
          </div>
        </div>
        <div>
          <div class="footer-heading">功能</div>
          <div class="footer-links">
            <router-link to="/pages">页面中心</router-link>
            <router-link to="/create">创建页面</router-link>
          </div>
        </div>
        <div>
          <div class="footer-heading">关于</div>
          <div class="footer-links">
            <span>Astaros Wiki</span>
            <span>© 2026</span>
          </div>
        </div>
      </div>
      <div class="app-footer-bottom">
        <span>Astaros Wiki · Vue 3 + Spring Boot</span>
        <span>MIT License · 2026</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from './stores/user'
import { Search } from '@element-plus/icons-vue'
import Sidebar from './components/Sidebar.vue'

const router = useRouter()
const userStore = useUserStore()
const searchKeyword = ref('')
const theme = ref<'dark' | 'light'>('dark')

onMounted(() => {
  const stored = localStorage.getItem('hermes-theme')
  if (stored === 'light') setTheme('light')
  else setTheme('dark')
})

function setTheme(t: 'dark' | 'light') {
  theme.value = t
  document.documentElement.setAttribute('data-theme', t)
  localStorage.setItem('hermes-theme', t)
}
function toggleTheme() { setTheme(theme.value === 'dark' ? 'light' : 'dark') }
function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value.trim() } })
    searchKeyword.value = ''
  }
}
function handleLogout() { userStore.logout(); router.push('/') }
</script>
