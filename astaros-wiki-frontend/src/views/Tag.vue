<template>
  <div>
    <div class="hw-card">
      <div class="hw-eyebrow">标签：{{ tagName }}</div>
      <div class="hw-card-content">包含该标签的所有页面</div>
    </div>

    <div v-if="loading">加载中...</div>
    <div v-else-if="pages.length === 0">
      <el-empty description="该标签下暂无页面" />
    </div>
    <ul v-else class="hw-list">
      <li v-for="page in pages" :key="page.id" class="hw-list-item">
        <router-link
          :to="`/page/${page.slug}`"
          style="color: var(--var(--color-foreground)); text-decoration: none"
        >
          <div style="font-weight: bold; font-size: 18px">{{ page.title }}</div>
          <div style="color: var(--var(--color-muted)); font-size: 14px">
            {{ page.categoryName || '未分类' }} | 作者：{{ page.authorName }} |
            {{ formatDate(page.updatedAt) }}
          </div>
        </router-link>
      </li>
    </ul>

    <div v-if="totalPages > 1" class="hw-pagination">
      <el-button :disabled="currentPage === 0" @click="handlePageChange(currentPage - 1)">
        上一页
      </el-button>
      <span style="padding: 8px 16px">第 {{ currentPage + 1 }} / {{ totalPages }} 页</span>
      <el-button
        :disabled="currentPage >= totalPages - 1"
        @click="handlePageChange(currentPage + 1)"
      >
        下一页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPagesByTag } from '../api/page'
import type { PageDTO } from '../api/page'

const route = useRoute()
const tagName = ref('')
const pages = ref<PageDTO[]>([])
const loading = ref(true)
const currentPage = ref(0)
const totalPages = ref(0)

onMounted(async () => {
  tagName.value = route.params.name as string
  await loadPages()
})

const loadPages = async () => {
  loading.value = true
  try {
    const res = await getPagesByTag(tagName.value, currentPage.value, 10)
    if (res.code === 200) {
      pages.value = res.data.content
      totalPages.value = res.data.totalPages
    }
  } catch (error) {
    console.error('获取页面失败:', error)
  } finally {
    loading.value = false
  }
}

const handlePageChange = async (page: number) => {
  currentPage.value = page
  await loadPages()
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>
