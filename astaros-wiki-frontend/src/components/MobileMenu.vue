<template>
  <div class="mob-float">
    <!-- Trigger button -->
    <button class="mob-btn" :class="{ open }" @click="open = !open">
      <span class="mob-btn-icon" v-if="!open">☰</span>
      <span class="mob-btn-icon" v-else>✕</span>
    </button>

    <!-- Slide-up panel -->
    <Transition name="mob-panel">
      <div v-if="open" class="mob-panel">
        <div class="mob-panel-inner">
          <!-- Quick nav links -->
          <div class="mob-links">
            <router-link to="/" class="mob-link" @click="open=false">首页</router-link>
            <router-link to="/search" class="mob-link" @click="open=false">搜索</router-link>
            <router-link v-if="userStore.isLoggedIn()" to="/pages" class="mob-link" @click="open=false">页面</router-link>
            <router-link v-if="userStore.isLoggedIn()" to="/profile" class="mob-link" @click="open=false">个人</router-link>
            <router-link v-if="userStore.isAdmin()" to="/admin" class="mob-link" @click="open=false">管理</router-link>
            <router-link v-if="!userStore.isLoggedIn()" to="/login" class="mob-link" @click="open=false">登录</router-link>
          </div>
          <!-- Categories -->
          <div class="mob-cats" v-if="categoryStore.categories.length">
            <div class="mob-cats-title">分类导航</div>
            <router-link
              v-for="c in categoryStore.categories"
              :key="c.id"
              :to="`/category/${c.id}`"
              class="mob-cat"
              @click="open=false"
            >{{ c.name }}</router-link>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '../stores/user'
import { useCategoryStore } from '../stores/category'

const userStore = useUserStore()
const categoryStore = useCategoryStore()
const open = ref(false)

onMounted(async () => { await categoryStore.fetchCategories() })
</script>

<style scoped>
.mob-float { display: none; position: fixed; bottom: 20px; left: 20px; z-index: 200; }
@media (max-width: 1024px) { .mob-float { display: block; } }

.mob-btn {
  width: 48px; height: 48px; border-radius: 50%;
  background: var(--fg); color: var(--bg); border: none;
  font-size: 18px; cursor: pointer; display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 20px rgba(0,0,0,.3); transition: transform .2s, box-shadow .2s;
}
.mob-btn:hover { transform: scale(1.06); box-shadow: 0 6px 28px rgba(0,0,0,.4); }
.mob-btn.open { background: var(--card); color: var(--fg); border: 1px solid var(--border); }
.mob-btn-icon { line-height: 1; }

.mob-panel {
  position: fixed; bottom: 80px; left: 20px; right: 20px; z-index: 199;
  max-height: 60vh; overflow-y: auto;
  background: var(--card); border: 1px solid var(--border);
  box-shadow: 0 12px 40px rgba(0,0,0,.35); padding: 16px;
}
.mob-panel-inner { display: flex; flex-direction: column; gap: 16px; }

.mob-links { display: flex; flex-wrap: wrap; gap: 6px; }
.mob-link {
  display: block; padding: 8px 16px; font-family: var(--mono); font-size: 11px;
  text-transform: uppercase; letter-spacing: .08em; border: 1px solid var(--border);
  color: var(--fg); transition: all .15s; text-decoration: none;
}
.mob-link:hover { background: color-mix(in srgb, var(--fg) 6%, transparent); }
.mob-link.router-link-active { border-color: var(--fg); background: color-mix(in srgb, var(--fg) 8%, transparent); }

.mob-cats-title {
  font-family: var(--mono); font-size: 10px; text-transform: uppercase;
  letter-spacing: .14em; color: var(--muted); margin-bottom: 6px;
}
.mob-cat {
  display: block; padding: 6px 0; font-size: 13px; color: var(--fg);
  border-bottom: 1px solid color-mix(in srgb, var(--fg) 4%, transparent);
  text-decoration: none; transition: padding-left .15s;
}
.mob-cat:last-child { border-bottom: none; }
.mob-cat:hover { padding-left: 6px; }

.mob-panel-enter-active { transition: all .2s ease-out; }
.mob-panel-leave-active { transition: all .12s ease-in; }
.mob-panel-enter-from, .mob-panel-leave-to { opacity: 0; transform: translateY(16px); }
</style>
