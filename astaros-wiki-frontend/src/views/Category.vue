<template>
  <div class="category-page">
    <div class="category-header">
      <p class="hw-eyebrow">分类</p>
      <h1 class="hw-section-title">{{ displayCategory?.name || '分类页面' }}</h1>
      <p class="text-muted" v-if="displayCategory?.description">
        {{ displayCategory.description }}
      </p>
    </div>

    <!-- Sub-category tabs -->
    <div v-if="showTabs" class="category-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        class="tab-btn"
        :class="{ active: activeTabId === tab.id }"
        @click="handleTabClick(tab)"
      >
        {{ tab.name }}
      </button>
    </div>

    <hr class="hw-divider" />

    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <div class="loading-pulse"></div>
    </div>

    <!-- Empty -->
    <div v-else-if="pages.length === 0" class="empty-state">
      <p class="hw-eyebrow">0 页面</p>
      <h2 class="hw-section-title">暂无页面</h2>
      <p class="text-muted">该分类下暂未发布任何页面。</p>
    </div>

    <!-- Page list -->
    <div v-else class="pages-grid">
      <router-link
        v-for="page in pages"
        :key="page.id"
        :to="`/page/${page.slug}`"
        class="hermes-card page-card"
      >
        <h3 class="page-title">{{ page.title }}</h3>
        <div class="page-meta">
          <span class="text-muted">作者：{{ page.authorName }}</span>
          <span class="text-muted">{{ formatDate(page.updatedAt) }}</span>
        </div>
      </router-link>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="pagination-row">
      <button
        class="hw-btn-ghost"
        :disabled="currentPage === 0"
        @click="changePage(currentPage - 1)"
      >
        ← 上一页
      </button>
      <span class="text-muted" style="font-family: var(--font-mono); font-size: 12px">
        {{ currentPage + 1 }} / {{ totalPages }}
      </span>
      <button
        class="hw-btn-ghost"
        :disabled="currentPage >= totalPages - 1"
        @click="changePage(currentPage + 1)"
      >
        下一页 →
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategoryById } from '../api/category'
import { getPagesByCategory } from '../api/page'
import type { CategoryDTO } from '../api/category'
import type { PageDTO } from '../api/page'

const route = useRoute()
const router = useRouter()
const category = ref<CategoryDTO | null>(null)
const parentCategory = ref<CategoryDTO | null>(null)
const pages = ref<PageDTO[]>([])
const loading = ref(true)
const currentPage = ref(0)
const totalPages = ref(0)

const displayCategory = computed(() => parentCategory.value || category.value)

const showTabs = computed(() => {
  const children = parentCategory.value?.children || category.value?.children || []
  return children.length > 0
})

const tabs = computed(() => {
  const children = parentCategory.value?.children || category.value?.children || []
  const allTabId = parentCategory.value?.id || category.value?.id || 0
  return [{ id: allTabId, name: '全部' } as CategoryDTO, ...children]
})

const activeTabId = computed(() => category.value?.id || 0)

onMounted(async () => {
  await loadCategory(Number(route.params.id))
})

watch(
  () => route.params.id,
  async (newId) => {
    currentPage.value = 0
    await loadCategory(Number(newId))
  },
)

async function loadCategory(id: number) {
  loading.value = true
  try {
    const res = await getCategoryById(id)
    if (res.code === 200) {
      category.value = res.data
      parentCategory.value = null
      if (category.value.parentId) {
        const parentRes = await getCategoryById(category.value.parentId)
        if (parentRes.code === 200) parentCategory.value = parentRes.data
      }
      await loadPages(id)
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  } finally {
    loading.value = false
  }
}

async function loadPages(categoryId: number) {
  try {
    const res = await getPagesByCategory(categoryId, currentPage.value, 10)
    if (res.code === 200) {
      pages.value = res.data.content
      totalPages.value = res.data.totalPages
    }
  } catch (error) {
    console.error('获取页面失败:', error)
  }
}

async function changePage(page: number) {
  currentPage.value = page
  await loadPages(activeTabId.value)
}

function handleTabClick(tab: CategoryDTO) {
  currentPage.value = 0
  router.push(`/category/${tab.id}`)
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.category-page {
  max-width: var(--max-width-container);
  margin: 0 auto;
  padding: 32px 24px;
}
.category-header {
  margin-bottom: 24px;
}
.category-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 0;
}
.tab-btn {
  padding: 8px 16px;
  font-family: var(--font-mono);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-muted-foreground);
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  transition: all 0.15s;
}
.tab-btn:hover {
  color: var(--color-foreground);
}
.tab-btn.active {
  color: var(--color-foreground);
  border-bottom-color: var(--color-foreground);
}
.pages-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.page-card {
  display: block;
}
.page-title {
  font-family: var(--font-expanded);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: var(--color-foreground);
  margin-bottom: 6px;
}
.page-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
}
.pagination-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border);
}
.pagination-row .btn-hermes-outline:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}
.loading-state {
  display: flex;
  justify-content: center;
  padding: 48px 0;
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
.empty-state {
  text-align: center;
  padding: 64px 0;
}
@media (max-width: 640px) {
  .category-page {
    padding: 16px 12px;
  }
}
</style>
