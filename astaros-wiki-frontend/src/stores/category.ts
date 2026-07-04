import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { CategoryDTO } from '../api/category'
import { getAllCategories } from '../api/category'

export const useCategoryStore = defineStore('category', () => {
  const categories = ref<CategoryDTO[]>([])
  const loading = ref(false)

  const fetchCategories = async () => {
    loading.value = true
    try {
      const res = await getAllCategories()
      if (res && res.code === 200) {
        categories.value = res.data
      }
    } catch (error) {
      console.error('获取分类失败:', error)
    } finally {
      loading.value = false
    }
  }

  return {
    categories,
    loading,
    fetchCategories,
  }
})
