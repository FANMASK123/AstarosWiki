import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { PageDTO } from '../api/page'
import { getRecentPages, searchPages } from '../api/page'

export const usePageStore = defineStore('page', () => {
  const recentPages = ref<PageDTO[]>([])
  const searchResults = ref<PageDTO[]>([])
  const loading = ref(false)

  // 获取最新页面
  const fetchRecentPages = async () => {
    loading.value = true
    try {
      const res = await getRecentPages()
      if (res.code === 200) {
        recentPages.value = res.data
      }
    } catch (error) {
      console.error('获取最新页面失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 搜索页面
  const search = async (keyword: string) => {
    loading.value = true
    try {
      const res = await searchPages(keyword)
      if (res.code === 200) {
        searchResults.value = res.data.content
      }
    } catch (error) {
      console.error('搜索失败:', error)
    } finally {
      loading.value = false
    }
  }

  return {
    recentPages,
    searchResults,
    loading,
    fetchRecentPages,
    search,
  }
})
