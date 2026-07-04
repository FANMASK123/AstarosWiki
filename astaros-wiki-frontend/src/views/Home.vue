<template>
  <div class="home">
    <!-- Hero -->
    <section class="home-hero">
      <p class="hw-eyebrow">知识库 · 开源</p>
      <h1 class="hw-hero-title">
        <span>阿斯塔洛斯</span>
        <span>Wiki</span>
      </h1>
      <p class="home-hero-desc">
        探索四大本源力量 — 神圣、深渊、黑暗、魔力 — 交织的奇幻世界
      </p>
      <div class="hw-hero-actions">
        <router-link to="/search" class="hw-btn-primary">
          开始探索 <span>→</span>
        </router-link>
        <router-link v-if="userStore.isLoggedIn()" to="/create" class="hw-btn-ghost">
          创建页面 <span>↗</span>
        </router-link>
      </div>
    </section>

    <!-- Main grid -->
    <section class="home-grid">
      <!-- Categories -->
      <div>
        <p class="hw-eyebrow">浏览</p>
        <h2 class="hw-section-title">分类导航</h2>
        <div v-if="categoryStore.loading" class="hw-skeleton">
          <div class="hw-skeleton-line w-75"></div>
          <div class="hw-skeleton-line w-50"></div>
          <div class="hw-skeleton-line w-100"></div>
        </div>
        <div v-else class="home-cat-grid">
          <div
            v-for="category in categoryStore.categories"
            :key="category.id"
            class="hw-card home-cat-card"
            @click="handleCategoryClick(category)"
          >
            <h3 class="hw-card-title">{{ category.name }}</h3>
            <p class="hw-mono">{{ category.description || '暂无描述' }}</p>
            <span v-if="category.children?.length" class="hw-badge" style="margin-top:8px">
              {{ category.children.length }} 子类
            </span>
          </div>
        </div>
      </div>

      <!-- Recent -->
      <div>
        <p class="hw-eyebrow">最新</p>
        <h2 class="hw-section-title">最近更新</h2>
        <div v-if="pageStore.loading" class="hw-skeleton">
          <div class="hw-skeleton-line w-100"></div>
          <div class="hw-skeleton-line w-75"></div>
          <div class="hw-skeleton-line w-50"></div>
        </div>
        <div v-else class="home-recent-list">
          <router-link
            v-for="page in pageStore.recentPages"
            :key="page.id"
            :to="`/page/${page.slug}`"
            class="hw-card home-recent-item"
          >
            <h3 class="hw-card-title">{{ page.title }}</h3>
            <div style="display:flex;align-items:center;gap:8px;margin-top:8px">
              <span class="hw-tag" v-if="page.categoryName">{{ page.categoryName }}</span>
              <span class="hw-mono">{{ formatDate(page.updatedAt) }}</span>
            </div>
          </router-link>
        </div>
      </div>
    </section>

    <!-- Quick links -->
    <section class="home-quick">
      <p class="hw-eyebrow">快捷入口</p>
      <h2 class="hw-section-title">热门主题</h2>
      <div class="home-quick-grid">
        <router-link to="/page/夜族" class="hw-card home-quick-card">
          <h3 class="hw-card-title">夜族</h3>
          <p class="hw-mono">探索夜族的神秘世界</p>
        </router-link>
        <router-link to="/page/仙灵" class="hw-card home-quick-card">
          <h3 class="hw-card-title">仙灵</h3>
          <p class="hw-mono">了解仙灵的优雅本质</p>
        </router-link>
        <router-link to="/page/龙族" class="hw-card home-quick-card">
          <h3 class="hw-card-title">龙族</h3>
          <p class="hw-mono">龙族的威严与力量</p>
        </router-link>
        <router-link to="/page/神圣" class="hw-card home-quick-card">
          <h3 class="hw-card-title">神圣本源</h3>
          <p class="hw-mono">神圣力量的奥秘</p>
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCategoryStore } from '../stores/category'
import { usePageStore } from '../stores/page'
import { useUserStore } from '../stores/user'
import type { CategoryDTO } from '../api/category'

const router = useRouter()
const categoryStore = useCategoryStore()
const pageStore = usePageStore()
const userStore = useUserStore()

onMounted(() => {
  categoryStore.fetchCategories()
  pageStore.fetchRecentPages()
})

function handleCategoryClick(data: CategoryDTO) {
  router.push(`/category/${data.id}`)
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString('zh-CN', { year: 'numeric', month: 'short', day: 'numeric' })
}
</script>

<style scoped>
.home { max-width: 1400px; margin: 0 auto; padding: 0 24px; }
.home-hero { padding: 80px 0 56px; border-bottom: 1px solid var(--color-border); margin-bottom: 56px; }
.home-hero-desc { margin-top: 16px; font-size: 1.1rem; color: var(--color-muted); max-width: 600px; line-height: 1.7; }
.home-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 48px; margin-bottom: 64px; }
.home-cat-grid { display: flex; flex-direction: column; gap: 8px; }
.home-cat-card { cursor: pointer; }
.home-recent-list { display: flex; flex-direction: column; gap: 8px; }
.home-recent-item { display: block; }
.home-quick { padding-top: 48px; border-top: 1px solid var(--color-border); padding-bottom: 64px; }
.home-quick-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 8px; }
.home-quick-card { text-align: center; padding: 32px 20px; }

@media (max-width: 1024px) { .home-grid { grid-template-columns: 1fr; } }
@media (max-width: 640px) {
  .home { padding: 0 12px; }
  .home-hero { padding: 48px 0 32px; }
  .home-quick-grid { grid-template-columns: 1fr 1fr; }
  .hw-hero-actions { flex-direction: column; }
}
</style>
