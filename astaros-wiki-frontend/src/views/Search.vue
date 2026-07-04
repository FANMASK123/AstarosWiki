<template>
  <div class="search-page">
    <div class="search-header">
      <p class="hw-eyebrow">搜索</p>
      <h1 class="hw-section-title">搜索结果</h1>
    </div>

    <!-- Search box -->
    <div class="search-box">
      <el-input
        v-model="keyword"
        placeholder="输入关键词搜索页面..."
        size="large"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <div class="loading-pulse"></div>
    </div>

    <!-- Results -->
    <div v-else-if="results.length > 0" class="search-results">
      <p
        class="text-muted"
        style="
          font-family: var(--font-mono);
          font-size: 12px;
          text-transform: uppercase;
          letter-spacing: 0.08em;
          margin-bottom: 16px;
        "
      >
        找到 {{ totalElements }} 条结果
      </p>
      <div class="results-list">
        <router-link
          v-for="page in results"
          :key="page.id"
          :to="`/page/${page.slug}`"
          class="hermes-card result-item"
        >
          <h3 class="result-title">{{ page.title }}</h3>
          <div class="result-meta">
            <span class="hw-tag" v-if="page.categoryName">{{ page.categoryName }}</span>
            <span class="text-muted">作者：{{ page.authorName }}</span>
            <span class="text-muted">{{ formatDate(page.updatedAt) }}</span>
          </div>
          <p class="result-excerpt">{{ truncateContent(page.content) }}</p>
        </router-link>
      </div>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="search-pagination">
        <button
          class="hw-btn-ghost"
          :disabled="currentPage === 0"
          @click="goToPage(currentPage - 1)"
        >
          ← 上一页
        </button>
        <span class="text-muted" style="font-family: var(--font-mono); font-size: 12px">
          {{ currentPage + 1 }} / {{ totalPages }}
        </span>
        <button
          class="hw-btn-ghost"
          :disabled="currentPage >= totalPages - 1"
          @click="goToPage(currentPage + 1)"
        >
          下一页 →
        </button>
      </div>
    </div>

    <!-- Empty -->
    <div v-else-if="searched" class="empty-state">
      <p class="hw-eyebrow">0 结果</p>
      <h2 class="hw-section-title">未找到相关页面</h2>
      <p class="text-muted">尝试使用其他关键词搜索。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { searchPages } from '../api/page'
import type { PageDTO } from '../api/page'
import { Search } from '@element-plus/icons-vue'

const route = useRoute()
const keyword = ref('')
const results = ref<PageDTO[]>([])
const loading = ref(false)
const searched = ref(false)
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

onMounted(() => {
  if (route.query.keyword) {
    keyword.value = route.query.keyword as string
    handleSearch()
  }
})

watch(
  () => route.query.keyword,
  (newKeyword) => {
    if (newKeyword) {
      keyword.value = newKeyword as string
      handleSearch()
    }
  },
)

async function handleSearch() {
  if (!keyword.value.trim()) return
  loading.value = true
  searched.value = true
  currentPage.value = 0
  try {
    const res = await searchPages(keyword.value.trim(), 0, 10)
    if (res.code === 200) {
      results.value = res.data.content
      totalPages.value = res.data.totalPages
      totalElements.value = res.data.total
    }
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

async function goToPage(page: number) {
  currentPage.value = page
  loading.value = true
  try {
    const res = await searchPages(keyword.value.trim(), page, 10)
    if (res.code === 200) {
      results.value = res.data.content
    }
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString('zh-CN')
}

function truncateContent(content: string) {
  return content.replace(/<[^>]*>/g, '').substring(0, 150) + '...'
}
</script>

<style scoped>
.search-page {
  max-width: var(--max-width-container);
  margin: 0 auto;
  padding: 32px 24px;
}
.search-header {
  margin-bottom: 24px;
}
.search-box {
  max-width: 640px;
  margin-bottom: 32px;
}
.results-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.result-item {
  display: block;
}
.result-title {
  font-family: var(--font-expanded);
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: var(--color-foreground);
  margin-bottom: 8px;
}
.result-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 13px;
}
.result-excerpt {
  font-size: 14px;
  color: var(--color-muted-foreground);
  line-height: 1.6;
}
.search-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border);
}
.search-pagination .btn-hermes-outline:disabled {
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
  .search-page {
    padding: 16px 12px;
  }
}
</style>
