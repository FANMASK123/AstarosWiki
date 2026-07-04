<template>
  <div class="home">
    <!-- Hero section -->
    <section class="hero">
      <div class="hero-content">
        <p class="section-label">知识库</p>
        <h1 class="hero-title">阿斯塔洛斯 Wiki</h1>
        <p class="hero-subtitle">
          探索充满神秘与奇幻的阿斯塔洛斯世界。四大本源力量——神圣、深渊、黑暗、魔力——
          交织成这个世界的基础，塑造着每一个生命和命运的轨迹。
        </p>
        <div class="hero-actions">
          <router-link to="/search" class="btn-hermes-primary">
            <span>🔍</span> 开始探索
          </router-link>
          <router-link to="/create" class="btn-hermes-outline" v-if="userStore.isLoggedIn()">
            创建页面 <span>↗</span>
          </router-link>
        </div>
      </div>
    </section>

    <!-- Main grid -->
    <section class="home-grid">
      <!-- Categories -->
      <div class="home-panel">
        <div class="panel-header">
          <p class="section-label">浏览</p>
          <h2 class="section-title">分类导航</h2>
        </div>
        <div v-if="categoryStore.loading" class="loading-state">
          <div class="loading-pulse"></div>
        </div>
        <div v-else class="category-grid">
          <div
            v-for="category in categoryStore.categories"
            :key="category.id"
            class="hermes-card category-card"
            @click="handleCategoryClick(category)"
          >
            <div class="category-card-header">
              <h3 class="category-name">{{ category.name }}</h3>
              <span v-if="category.children?.length" class="category-count">
                {{ category.children.length }} 子类
              </span>
            </div>
            <p class="category-desc">{{ category.description || '暂无描述' }}</p>
            <div
              v-if="category.children?.length && expandedCategories.includes(category.id)"
              class="sub-categories"
            >
              <span
                v-for="child in category.children"
                :key="child.id"
                class="hermes-tag"
                @click.stop="handleCategoryClick(child)"
              >
                {{ child.name }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent pages -->
      <div class="home-panel">
        <div class="panel-header">
          <p class="section-label">最新</p>
          <h2 class="section-title">最近更新</h2>
        </div>
        <div v-if="pageStore.loading" class="loading-state">
          <div class="loading-pulse"></div>
        </div>
        <div v-else class="recent-list">
          <router-link
            v-for="page in pageStore.recentPages"
            :key="page.id"
            :to="`/page/${page.slug}`"
            class="hermes-card recent-item"
          >
            <h3 class="recent-title">{{ page.title }}</h3>
            <div class="recent-meta">
              <span class="hermes-tag" v-if="page.categoryName">{{ page.categoryName }}</span>
              <span class="text-muted">{{ formatDate(page.updatedAt) }}</span>
            </div>
          </router-link>
        </div>
      </div>
    </section>

    <!-- Quick links -->
    <section class="quick-section">
      <p class="section-label">快捷入口</p>
      <h2 class="section-title">热门主题</h2>
      <div class="quick-grid">
        <router-link to="/page/夜族" class="hermes-card quick-card">
          <h3 class="hermes-card-title">夜族</h3>
          <p class="text-muted">探索夜族的神秘世界</p>
        </router-link>
        <router-link to="/page/仙灵" class="hermes-card quick-card">
          <h3 class="hermes-card-title">仙灵</h3>
          <p class="text-muted">了解仙灵的优雅本质</p>
        </router-link>
        <router-link to="/page/龙族" class="hermes-card quick-card">
          <h3 class="hermes-card-title">龙族</h3>
          <p class="text-muted">龙族的威严与力量</p>
        </router-link>
        <router-link to="/page/神圣" class="hermes-card quick-card">
          <h3 class="hermes-card-title">神圣本源</h3>
          <p class="text-muted">神圣力量的奥秘</p>
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCategoryStore } from '../stores/category'
import { usePageStore } from '../stores/page'
import { useUserStore } from '../stores/user'
import type { CategoryDTO } from '../api/category'

const router = useRouter()
const categoryStore = useCategoryStore()
const pageStore = usePageStore()
const userStore = useUserStore()

const expandedCategories = ref<number[]>([])

onMounted(() => {
  categoryStore.fetchCategories()
  pageStore.fetchRecentPages()
})

function handleCategoryClick(data: CategoryDTO) {
  if (data.children && data.children.length > 0) {
    const idx = expandedCategories.value.indexOf(data.id)
    if (idx > -1) {
      expandedCategories.value.splice(idx, 1)
    } else {
      expandedCategories.value.push(data.id)
    }
  } else {
    router.push(`/category/${data.id}`)
  }
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}
</script>

<style scoped>
.home {
  max-width: var(--max-width-container);
  margin: 0 auto;
  padding: 0 24px;
}

/* Hero */
.hero {
  padding: 64px 0 48px;
  border-bottom: 1px solid var(--color-border);
  margin-bottom: 48px;
}

.hero-content {
  max-width: 720px;
}

.hero-title {
  font-family: var(--font-expanded);
  font-size: clamp(2.5rem, 6vw, 5rem);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.02em;
  line-height: 0.95;
  margin-top: 12px;
  color: var(--color-foreground);
}

.hero-subtitle {
  margin-top: 20px;
  font-size: 18px;
  line-height: 1.7;
  color: var(--color-muted-foreground);
  max-width: 640px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-top: 32px;
}

/* Grid */
.home-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  margin-bottom: 64px;
}

.panel-header {
  margin-bottom: 24px;
}

/* Category grid */
.category-grid {
  display: grid;
  gap: 8px;
}

.category-card {
  cursor: pointer;
}

.category-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.category-name {
  font-family: var(--font-expanded);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--color-foreground);
}

.category-count {
  font-family: var(--font-mono);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-muted-foreground);
}

.category-desc {
  font-size: 14px;
  color: var(--color-muted-foreground);
  line-height: 1.5;
}

.sub-categories {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* Recent list */
.recent-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-item {
  display: block;
}

.recent-title {
  font-family: var(--font-expanded);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: var(--color-foreground);
  margin-bottom: 6px;
  transition: color 0.15s;
}
.recent-item:hover .recent-title {
  opacity: 0.8;
}

.recent-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Quick links */
.quick-section {
  padding-bottom: 64px;
  border-top: 1px solid var(--color-border);
  padding-top: 48px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 8px;
}

.quick-card {
  display: block;
  text-align: center;
  padding: 32px 20px;
}

/* Loading */
.loading-state {
  display: flex;
  justify-content: center;
  padding: 48px;
}

.loading-pulse {
  width: 24px;
  height: 24px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-foreground);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Responsive */
@media (max-width: 1024px) {
  .home-grid {
    grid-template-columns: 1fr;
    gap: 32px;
  }
}

@media (max-width: 640px) {
  .home {
    padding: 0 12px;
  }
  .hero {
    padding: 40px 0 32px;
  }
  .hero-title {
    font-size: clamp(2rem, 8vw, 3rem);
  }
  .hero-actions {
    flex-direction: column;
  }
  .quick-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
