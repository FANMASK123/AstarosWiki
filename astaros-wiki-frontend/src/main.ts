// ============================================================
// Vue 3 应用入口文件 - main.ts
// ============================================================
//
// 【Vue 3 知识点】
// 1. createApp: 创建 Vue 应用实例
// 2. app.use(): 注册插件
// 3. app.mount(): 挂载应用到 DOM
//
// 【推荐阅读】
// https://cn.vuejs.org/guide/essentials/application.html
// ============================================================

// --------------------------------------------------------
// 导入 Vue 核心函数
// --------------------------------------------------------
// createApp: Vue 3 创建应用的函数
import { createApp } from 'vue'

// --------------------------------------------------------
// 导入 Pinia 状态管理
// --------------------------------------------------------
// Pinia: Vue 3 官方推荐的状态管理库
// 【学习要点】替代 Vuex，更简单更灵活
import { createPinia } from 'pinia'

// --------------------------------------------------------
// 导入 Element Plus UI 库
// --------------------------------------------------------
// Element Plus: Vue 3 的组件库
// 提供丰富的 UI 组件（按钮、表格、表单等）
import ElementPlus from 'element-plus'

// --------------------------------------------------------
// 导入 Element Plus CSS 样式
// --------------------------------------------------------
// 必须导入 CSS 才能正确显示组件样式
import 'element-plus/dist/index.css'
import 'highlight.js/styles/atom-one-dark.css'

// --------------------------------------------------------
// 导入 Element Plus 图标库
// --------------------------------------------------------
// Element Plus Icons: 图标组件库
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// --------------------------------------------------------
// 导入 Vue Router 路由
// --------------------------------------------------------
// Vue Router: Vue 官方路由管理器
import router from './router'

// --------------------------------------------------------
// 导入 Hermes 主题样式（替换旧的 classic-theme）
// --------------------------------------------------------
// hermes-theme.css: 基于 hermes-ai.net 设计系统的暗色主题
import './assets/hermes-theme.css'

// --------------------------------------------------------
// 导入根组件
// --------------------------------------------------------
// App.vue: 应用的根组件
import App from './App.vue'

// ========================================================
// 创建 Vue 应用实例
// ========================================================
// createApp(App): 创建应用，App 是根组件
// 【学习要点】Vue 3 使用 createApp，Vue 2 使用 new Vue()
const app = createApp(App)

// ========================================================
// 注册 Element Plus 图标组件
// ========================================================
// 【学习要点】全局注册所有图标组件，可在任何组件中使用
//
// for (const [key, component] of Object.entries(icons)):
// - Object.entries(): 将对象转为键值对数组
// - app.component(key, component): 注册全局组件
//
// 使用方式：<el-icon><Search /></el-icon>
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// ========================================================
// 注册 Pinia 状态管理
// ========================================================
// app.use(createPinia()): 使用 Pinia 插件
// 【学习要点】Pinia 用于全局状态管理（如用户信息）
//
// 使用方式：
// import { useUserStore } from '@/stores/user'
// const userStore = useUserStore()
app.use(createPinia())

// ========================================================
// 注册 Element Plus UI 库
// ========================================================
// app.use(ElementPlus): 使用 Element Plus 插件
// 【学习要点】Element Plus 提供丰富的组件
//
// 使用方式：<el-button>按钮</el-button>
app.use(ElementPlus)

// ========================================================
// 注册 Vue Router
// ========================================================
// app.use(router): 使用路由插件
// 【学习要点】Vue Router 用于页面导航和路由管理
//
// 使用方式：<router-link to="/home">首页</router-link>
app.use(router)

// ========================================================
// 挂载应用到 DOM
// ========================================================
// app.mount('#app'): 挂载到 index.html 中的 <div id="app">
//
// 【学习要点】挂载后 Vue 应用开始运行
// index.html 中必须有：<div id="app"></div>
app.mount('#app')
