<template>
  <div>
    <div v-if="loading" class="loading"><div class="spinner"></div></div>
    <template v-else>
      <!-- Breadcrumb -->
      <div class="row mb-24" style="font-size:13px">
        <router-link to="/" class="text-muted" style="transition:color .15s">首页</router-link>
        <span class="text-muted">/</span>
        <template v-if="parentCat"><router-link :to="`/category/${parentCat.id}`" class="text-muted" style="transition:color .15s">{{ parentCat.name }}</router-link><span class="text-muted">/</span></template>
        <span style="color:var(--fg)">{{ cat?.name }}</span>
      </div>

      <p class="eyebrow">分类</p>
      <h1 class="section-title">{{ cat?.name || '分类' }}</h1>
      <p class="text-muted mb-24" v-if="cat?.description">{{ cat.description }}</p>

      <!-- Page list -->
      <div v-if="pages.length" class="stack">
        <router-link v-for="p in pages" :key="p.id" :to="`/page/${p.slug}`" class="card">
          <div class="card-title">{{ p.title }}</div>
          <div class="row mt-16"><span class="mono">{{ p.authorName }}</span><span class="mono">{{ fmt(p.updatedAt) }}</span></div>
        </router-link>
      </div>
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
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCategoryById } from '../api/category'
import { getPagesByCategory } from '../api/page'
import type { CategoryDTO } from '../api/category'
import type { PageDTO } from '../api/page'

const route = useRoute()
const cat = ref<CategoryDTO|null>(null); const parentCat = ref<CategoryDTO|null>(null)
const pages = ref<PageDTO[]>([]); const loading = ref(true)
const cur = ref(0); const totalPages = ref(0)

onMounted(async () => { await load(Number(route.params.id)) })
watch(() => route.params.id, async (v) => { cur.value = 0; await load(Number(v)) })

async function load(id: number) {
  loading.value = true
  try {
    const r = await getCategoryById(id); if (r.code===200) { cat.value = r.data; parentCat.value = null
      if (cat.value.parentId) { const pr = await getCategoryById(cat.value.parentId); if (pr.code===200) parentCat.value = pr.data }
    }
    const pr = await getPagesByCategory(id, cur.value, 10); if (pr.code===200) { pages.value = pr.data.content; totalPages.value = pr.data.totalPages }
  } catch(e) { console.error(e) } finally { loading.value = false }
}
async function go(p: number) { cur.value = p; await load(cat.value!.id) }
function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN') }
</script>
