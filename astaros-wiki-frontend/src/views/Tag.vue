<template>
  <div>
    <p class="eyebrow">标签：{{ tagName }}</p>
    <h2 class="section-title">包含该标签的页面</h2>
    <div v-if="loading" class="loading"><div class="spinner"></div></div>
    <div v-else-if="pages.length===0" class="empty"><h2 class="section-title">暂无页面</h2></div>
    <div v-else class="stack">
      <router-link v-for="p in pages" :key="p.id" :to="`/page/${p.slug}`" class="card">
        <div class="card-title">{{ p.title }}</div>
        <div class="row mt-16"><span class="mono">{{ p.categoryName || '未分类' }}</span><span class="mono">{{ p.authorName }}</span><span class="mono">{{ fmt(p.updatedAt) }}</span></div>
      </router-link>
    </div>
    <div v-if="totalPages>1" class="pagination-row">
      <button class="btn btn-ghost" :disabled="cur===0" @click="go(cur-1)">←</button>
      <span class="mono">{{ cur+1 }}/{{ totalPages }}</span>
      <button class="btn btn-ghost" :disabled="cur>=totalPages-1" @click="go(cur+1)">→</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPagesByTag } from '../api/page'
import type { PageDTO } from '../api/page'

const route = useRoute()
const tagName = ref(route.params.name as string)
const pages = ref<PageDTO[]>([])
const loading = ref(true)
const cur = ref(0)
const totalPages = ref(0)

onMounted(async () => { await load() })
async function load() {
  loading.value = true
  try { const r = await getPagesByTag(tagName.value, cur.value, 10); if (r.code===200) { pages.value=r.data.content; totalPages.value=r.data.totalPages } } catch(e) { console.error(e) } finally { loading.value=false }
}
async function go(p: number) { cur.value=p; await load() }
function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN') }
</script>
