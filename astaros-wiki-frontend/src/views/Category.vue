<template>
  <div>
    <div v-if="loading" class="loading"><div class="spinner"></div></div>
    <template v-else>
      <p class="eyebrow">分类</p>
      <h1 class="section-title">{{ display?.name || '分类' }}</h1>
      <p class="text-muted" v-if="display?.description">{{ display.description }}</p>
      <div v-if="tabs.length>1" style="display:flex;gap:0;margin:16px 0 20px">
        <button v-for="t in tabs" :key="t.id" class="btn btn-ghost" style="border-bottom:none;font-size:11px" :style="{fontWeight:t.id===activeId?'700':'400'}" @click="router.push(`/category/${t.id}`)">{{ t.name }}</button>
      </div>
      <div v-if="pages.length" class="stack"><router-link v-for="p in pages" :key="p.id" :to="`/page/${p.slug}`" class="card"><div class="card-title">{{ p.title }}</div><div class="row mt-16"><span class="mono">{{ p.authorName }}</span><span class="mono">{{ fmt(p.updatedAt) }}</span></div></router-link></div>
      <div v-else class="empty"><h2 class="section-title">暂无页面</h2><p class="text-muted">该分类下暂未发布页面</p></div>
      <div v-if="totalPages>1" class="pagination-row">
        <button class="btn btn-ghost" :disabled="cur===0" @click="go(cur-1)">←</button>
        <span class="mono">{{ cur+1 }}/{{ totalPages }}</span>
        <button class="btn btn-ghost" :disabled="cur>=totalPages-1" @click="go(cur+1)">→</button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategoryById } from '../api/category'
import { getPagesByCategory } from '../api/page'
import type { CategoryDTO } from '../api/category'
import type { PageDTO } from '../api/page'

const route = useRoute(); const router = useRouter()
const cat = ref<CategoryDTO|null>(null); const parent = ref<CategoryDTO|null>(null)
const pages = ref<PageDTO[]>([]); const loading = ref(true)
const cur = ref(0); const totalPages = ref(0)
const display = computed(() => parent.value || cat.value)
const tabs = computed(() => {
  const all = [{ id: parent.value?.id||cat.value?.id||0, name: '全部' } as CategoryDTO]
  return [...all, ...(parent.value?.children || cat.value?.children || [])]
})
const activeId = computed(() => cat.value?.id || 0)

onMounted(async () => { await load(Number(route.params.id)) })
watch(() => route.params.id, async (v) => { cur.value = 0; await load(Number(v)) })

async function load(id: number) {
  loading.value = true
  try {
    const r = await getCategoryById(id); if (r.code===200) { cat.value = r.data; parent.value = null; if (cat.value.parentId) { const pr = await getCategoryById(cat.value.parentId); if (pr.code===200) parent.value = pr.data } }
    const pr = await getPagesByCategory(id, cur.value, 10); if (pr.code===200) { pages.value = pr.data.content; totalPages.value = pr.data.totalPages }
  } catch(e) { console.error(e) } finally { loading.value = false }
}
async function go(p: number) { cur.value = p; await load(activeId.value) }
function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN') }
</script>
