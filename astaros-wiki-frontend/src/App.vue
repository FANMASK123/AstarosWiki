<template>
  <div id="app" class="app-shell">
    <div class="starfield">
      <div class="starfield-layer"><div class="starfield-dot" style="left:15%;top:10%;width:2px;height:2px"></div><div class="starfield-dot" style="left:35%;top:25%;width:1px;height:1px"></div><div class="starfield-dot" style="left:55%;top:8%;width:2px;height:2px;background:rgba(255,255,255,.3)"></div><div class="starfield-dot" style="left:75%;top:30%;width:1px;height:1px"></div><div class="starfield-dot" style="left:20%;top:45%;width:2px;height:2px"></div><div class="starfield-dot" style="left:50%;top:50%;width:3px;height:3px;background:rgba(255,255,255,.7)"></div><div class="starfield-dot" style="left:80%;top:55%;width:1px;height:1px"></div><div class="starfield-dot" style="left:10%;top:70%;width:2px;height:2px"></div><div class="starfield-dot" style="left:40%;top:75%;width:1px;height:1px"></div><div class="starfield-dot" style="left:65%;top:65%;width:2px;height:2px;background:rgba(255,255,255,.3)"></div><div class="starfield-dot" style="left:90%;top:80%;width:1px;height:1px"></div><div class="starfield-dot" style="left:30%;top:90%;width:2px;height:2px"></div></div>
      <div class="starfield-layer"><div class="starfield-dot" style="left:8%;top:15%;width:1px;height:1px"></div><div class="starfield-dot" style="left:28%;top:5%;width:2px;height:2px;background:rgba(200,220,255,.4)"></div><div class="starfield-dot" style="left:48%;top:20%;width:1px;height:1px"></div><div class="starfield-dot" style="left:68%;top:12%;width:2px;height:2px"></div><div class="starfield-dot" style="left:85%;top:35%;width:1px;height:1px"></div><div class="starfield-dot" style="left:12%;top:55%;width:2px;height:2px;background:rgba(200,220,255,.6)"></div><div class="starfield-dot" style="left:38%;top:42%;width:1px;height:1px"></div><div class="starfield-dot" style="left:58%;top:60%;width:2px;height:2px"></div><div class="starfield-dot" style="left:78%;top:48%;width:1px;height:1px"></div><div class="starfield-dot" style="left:22%;top:78%;width:2px;height:2px;background:rgba(200,220,255,.5)"></div><div class="starfield-dot" style="left:62%;top:85%;width:1px;height:1px"></div><div class="starfield-dot" style="left:92%;top:68%;width:2px;height:2px"></div></div>
      <div class="starfield-layer"><div class="starfield-dot" style="left:18%;top:35%;width:1px;height:1px"></div><div class="starfield-dot" style="left:42%;top:18%;width:2px;height:2px"></div><div class="starfield-dot" style="left:72%;top:42%;width:1px;height:1px;background:rgba(220,230,255,.5)"></div><div class="starfield-dot" style="left:5%;top:62%;width:2px;height:2px"></div><div class="starfield-dot" style="left:32%;top:58%;width:1px;height:1px"></div><div class="starfield-dot" style="left:52%;top:38%;width:2px;height:2px;background:rgba(220,230,255,.4)"></div><div class="starfield-dot" style="left:82%;top:72%;width:1px;height:1px"></div><div class="starfield-dot" style="left:15%;top:85%;width:2px;height:2px"></div><div class="starfield-dot" style="left:45%;top:92%;width:1px;height:1px"></div></div>
    </div>

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
      <aside class="app-sidebar"><Sidebar /></aside>
      <main class="app-content">
        <router-view v-slot="{ Component }">
          <Transition name="page" mode="out-in"><component :is="Component" /></Transition>
        </router-view>
      </main>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from './stores/user'
import { Search } from '@element-plus/icons-vue'
import Sidebar from './components/Sidebar.vue'
import Mailbox from './components/Mailbox.vue'

const router = useRouter(); const userStore = useUserStore()
const kw = ref(''); const mailOpen = ref(false); const t = ref<'dark'|'light'>('dark')

onMounted(() => { const s = localStorage.getItem('hermes-theme'); if (s === 'light') set('light'); else set('dark') })
function set(v:'dark'|'light') { t.value = v; document.documentElement.setAttribute('data-theme',v); localStorage.setItem('hermes-theme',v) }
function toggle() { set(t.value==='dark'?'light':'dark') }
function search() { if (kw.value.trim()) { router.push({path:'/search',query:{keyword:kw.value.trim()}}); kw.value='' } }
function logout() { userStore.logout(); router.push('/') }
</script>
