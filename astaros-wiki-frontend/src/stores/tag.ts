import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getAllTags, type Tag } from '../api/tag'

export const useTagStore = defineStore('tag', () => {
  const tags = ref<Tag[]>([])
  const loading = ref(false)

  const fetchTags = async () => {
    loading.value = true
    try {
      const res = await getAllTags()
      if (res.code === 200) {
        tags.value = res.data || []
      }
    } catch (error) {
      console.error('获取标签失败:', error)
    } finally {
      loading.value = false
    }
  }

  return { tags, loading, fetchTags }
})
