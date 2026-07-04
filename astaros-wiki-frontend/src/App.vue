<template>
  <div id="app" class="app-shell">
    <div class="starfield"></div>

    <header class="app-header">
      <router-link to="/" class="app-header-brand">阿斯塔洛斯 Wiki</router-link>
      <nav class="app-header-nav">
        <router-link to="/" class="nav-link" exact-active-class="active">首页</router-link>
        <template v-if="userStore.isLoggedIn()">
          <router-link to="/pages" class="nav-link" active-class="active">页面</router-link>
          <router-link to="/profile" class="nav-link" active-class="active">个人</router-link>
          <router-link v-if="userStore.isAdmin()" to="/admin" class="nav-link" active-class="active">管理</router-link>
          <a class="nav-link" style="cursor:pointer" @click="logout">退出</a>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-link" active-class="active">登录</router-link>
          <router-link to="/register" class="nav-link" active-class="active">注册</router-link>
        </template>
      </nav>
      <div class="app-header-right">
        <el-input v-model="kw" placeholder="搜索..." size="small" style="width:140px" @keyup.enter="search">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <button class="btn btn-ghost" style="padding:4px 10px;font-size:9px" @click="toggle">{{ t==='dark'?'LIGHT':'DARK' }}</button>
      </div>
    </header>

    <div class="app-main">
      <main class="app-content">
        <router-view v-slot="{ Component }">
          <Transition name="page" mode="out-in"><component :is="Component" /></Transition>
        </router-view>
      </main>
      <aside class="app-sidebar"><Sidebar /></aside>
    </div>

    <footer class="app-footer">
      <div class="app-footer-grid">
        <div><span style="font-weight:700;font-size:15px;text-transform:uppercase;letter-spacing:.06em">阿斯塔洛斯 Wiki</span><p class="text-muted mt-16">奇幻世界观知识库</p></div>
        <div><div class="footer-heading">导航</div><div class="footer-links"><router-link to="/">首页</router-link><router-link to="/search">搜索</router-link></div></div>
        <div><div class="footer-heading">功能</div><div class="footer-links"><router-link to="/pages">页面中心</router-link><router-link to="/create">创建页面</router-link></div></div>
        <div><div class="footer-heading">关于</div><div class="footer-links"><span>Astaros Wiki</span><span>© 2026</span></div></div>
      </div>
      <div class="app-footer-bottom"><span>Astaros Wiki · Vue 3 + Spring Boot</span><span>MIT License · 2026</span></div>
    </footer>

    <button v-if="userStore.isLoggedIn()" class="mailbox-trigger" @click="mailOpen=!mailOpen">✉</button>
    <div v-if="mailOpen && userStore.isLoggedIn()" class="mailbox-panel"><Mailbox @close="mailOpen=false" /></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from './stores/user'
import { Search } from '@element-plus/icons-vue'
import Sidebar from './components/Sidebar.vue'
import Mailbox from './components/Mailbox.vue'

const router = useRouter(); const userStore = useUserStore()
const kw = ref(''); const mailOpen = ref(false); const t = ref<'dark'|'light'>('dark')

onMounted(() => { const s = localStorage.getItem('hermes-theme'); if (s === 'light') set('light'); else set('dark'); document.addEventListener('mousemove', track) })
onUnmounted(() => { document.removeEventListener('mousemove', track) })
function track(e: MouseEvent) { const x = (e.clientX / window.innerWidth * 100).toFixed(1); const y = (e.clientY / window.innerHeight * 100).toFixed(1); document.documentElement.style.setProperty('--mx', x + '%'); document.documentElement.style.setProperty('--my', y + '%') }
function set(v:'dark'|'light') { t.value = v; document.documentElement.setAttribute('data-theme',v); localStorage.setItem('hermes-theme',v) }
function toggle() { set(t.value==='dark'?'light':'dark') }
function search() { if (kw.value.trim()) { router.push({path:'/search',query:{keyword:kw.value.trim()}}); kw.value='' } }
function logout() { userStore.logout(); router.push('/') }
</script>
