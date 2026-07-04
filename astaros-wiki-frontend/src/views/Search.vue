<template>
  <div>
    <p class="eyebrow">搜索</p>
    <div class="search-box"><el-input v-model="kw" placeholder="输入关键词搜索页面..." size="large" @keyup.enter="search"><template #prefix><el-icon><Search /></el-icon></template><template #append><el-button @click="search">搜索</el-button></template></el-input></div>
    <div v-if="loading" class="loading"><div class="spinner"></div></div>
    <p v-else-if="results.length" class="mono mb-16">找到 {{ total }} 条结果</p>
    <div v-if="results.length" class="stack">
      <router-link v-for="p in results" :key="p.id" :to="`/page/${p.slug}`" class="card search-result">
        <div class="search-result-title">{{ p.title }}</div>
        <div class="row mb-16">
          <span class="tag" v-if="p.categoryName">{{ p.categoryName }}</span>
          <span class="mono">{{ p.authorName }}</span>
          <span class="mono">{{ fmt(p.updatedAt) }}</span>
        </div>
        <p class="text-muted" style="font-size:14px;line-height:1.5">{{ strip(p.content).substring(0, 150) }}...</p>
      </router-link>
    </div>
    <div v-else-if="searched" class="empty"><p class="eyebrow">0 结果</p><h2 class="section-title">未找到</h2><p class="text-muted">尝试其他关键词</p></div>
    <div v-if="pages>1" class="pagination-row">
      <button class="btn btn-ghost" :disabled="page===0" @click="go(page-1)">← 上一页</button>
      <span class="mono">{{ page+1 }} / {{ pages }}</span>
      <button class="btn btn-ghost" :disabled="page>=pages-1" @click="go(page+1)">下一页 →</button>
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
const kw = ref('')
const results = ref<PageDTO[]>([])
const loading = ref(false)
const searched = ref(false)
const page = ref(0)
const pages = ref(0)
const total = ref(0)

onMounted(() => { if (route.query.keyword) { kw.value = route.query.keyword as string; search() } })
watch(() => route.query.keyword, (v) => { if (v) { kw.value = v as string; search() } })

async function search() { if (!kw.value.trim()) return; loading.value = true; searched.value = true; page.value = 0; await fetch() }
async function go(p: number) { page.value = p; loading.value = true; await fetch() }
async function fetch() {
  try { const r = await searchPages(kw.value.trim(), page.value, 10); if (r.code===200) { results.value = r.data.content; pages.value = r.data.totalPages; total.value = r.data.total } } catch(e) { console.error(e) } finally { loading.value = false }
}
function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN') }
function strip(s: string) { return s.replace(/<[^>]*>/g,'') }
</script>
