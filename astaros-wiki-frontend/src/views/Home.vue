<template>
  <div>
    <section class="hero">
      <p class="eyebrow">知识库 · 开源</p>
      <h1 class="hero-title"><span>阿斯塔洛斯</span><span>Wiki</span></h1>
      <p class="hero-sub">探索四大本源力量 — 神圣、深渊、黑暗、魔力 — 交织的奇幻世界</p>
      <div class="hero-actions">
        <router-link to="/search" class="btn btn-primary">开始探索 <span>→</span></router-link>
        <router-link v-if="userStore.isLoggedIn()" to="/create" class="btn btn-ghost">创建页面 <span>↗</span></router-link>
      </div>
    </section>

    <div class="mb-24">
      <div>
        <p class="eyebrow">最新</p>
        <h2 class="section-title">最近更新</h2>
        <div v-if="pageStore.loading" class="skeleton">
          <div class="skel-line skel-w100"></div><div class="skel-line skel-w75"></div><div class="skel-line skel-w50"></div>
        </div>
        <div v-else class="stack">
          <router-link v-for="p in pageStore.recentPages" :key="p.id" :to="`/page/${p.slug}`" class="card">
            <div class="card-title">{{ p.title }}</div>
            <div class="row mt-16">
              <span class="tag" v-if="p.categoryName">{{ p.categoryName }}</span>
              <span class="mono">{{ fmt(p.updatedAt) }}</span>
            </div>
          </router-link>
        </div>
      </div>
    </div>

    <p class="eyebrow">快捷入口</p>
    <h2 class="section-title">热门主题</h2>
    <div class="grid-auto">
      <router-link v-for="item in quickLinks" :key="item.slug" :to="`/page/${item.slug}`" class="card" style="text-align:center;padding:28px 16px">
        <div class="card-title">{{ item.name }}</div>
        <p class="mono">{{ item.desc }}</p>
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { usePageStore } from '../stores/page'
import { useUserStore } from '../stores/user'

const pageStore = usePageStore()
const userStore = useUserStore()

const quickLinks = [
  { slug: '夜族', name: '夜族', desc: '探索夜族的神秘世界' },
  { slug: '仙灵', name: '仙灵', desc: '了解仙灵的优雅本质' },
  { slug: '龙族', name: '龙族', desc: '龙族的威严与力量' },
  { slug: '神圣', name: '神圣本源', desc: '神圣力量的奥秘' },
]

onMounted(() => { pageStore.fetchRecentPages() })
function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: 'short', day: 'numeric' }) }
</script>
