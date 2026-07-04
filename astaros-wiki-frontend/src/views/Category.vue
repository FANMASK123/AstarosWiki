<template>
  <div>
    <div v-if="loading" class="loading"><div class="spinner"></div></div>
    <template v-else-if="cat">
      <!-- Breadcrumb -->
      <div class="row mb-24" style="font-size:12px;font-family:var(--mono);text-transform:uppercase;letter-spacing:.06em">
        <router-link to="/" class="text-muted" style="transition:color .15s">首页</router-link>
        <span class="text-muted">/</span>
        <template v-if="grandCat">
          <router-link :to="`/category/${grandCat.id}`" class="text-muted" style="transition:color .15s">{{ grandCat.name }}</router-link>
          <span class="text-muted">/</span>
        </template>
        <template v-if="parentCat">
          <router-link :to="`/category/${parentCat.id}`" class="text-muted" style="transition:color .15s">{{ parentCat.name }}</router-link>
          <span class="text-muted">/</span>
        </template>
        <span style="color:var(--fg)">{{ cat.name }}</span>
        <span v-if="depth===2" class="text-muted" style="font-size:10px;margin-left:4px">(二级分类)</span>
        <span v-else-if="depth>=3" class="text-muted" style="font-size:10px;margin-left:4px">(三级分类)</span>
      </div>

      <!-- Category header -->
      <p class="eyebrow">分类</p>
      <h1 v-if="depth===1" class="section-title">{{ cat.name }}</h1>
      <h2 v-else-if="depth===2" class="section-title" style="font-size:clamp(1.3rem,2.5vw,1.8rem)">{{ cat.name }}</h2>
      <h3 v-else class="section-title" style="font-size:clamp(1.1rem,2vw,1.5rem)">{{ cat.name }}</h3>
      <p class="text-muted mb-24" v-if="cat.description">{{ cat.description }}</p>

      <!-- Sub-categories tree -->
      <div v-if="cat.children?.length" class="mb-24">
        <p class="eyebrow">子分类</p>
        <div class="stack">
          <div v-for="ch in cat.children" :key="ch.id">
            <!-- Level 2 sub-category -->
            <router-link :to="`/category/${ch.id}`" class="card" style="padding:14px 18px">
              <div class="row">
                <span style="font-weight:700;font-size:14px;text-transform:uppercase;letter-spacing:.04em">{{ ch.name }}</span>
                <span class="badge" v-if="ch.children?.length">{{ ch.children.length }} 子类</span>
                <span class="mono">{{ ch.description || '' }}</span>
              </div>
            </router-link>
            <!-- Level 3 sub-categories -->
            <div v-if="ch.children?.length" style="padding-left:24px;margin-top:4px" class="stack">
              <router-link v-for="gc in ch.children" :key="gc.id" :to="`/category/${gc.id}`" class="card" style="padding:10px 16px;border-left:2px solid var(--border)">
                <div class="row">
                  <span style="font-weight:600;font-size:13px;text-transform:uppercase;letter-spacing:.03em">{{ gc.name }}</span>
                  <span class="mono">{{ gc.description || '' }}</span>
                </div>
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <div class="divider"></div>

      <!-- Pages -->
      <p class="eyebrow">页面</p>
      <div v-if="pages.length" class="stack">
        <router-link v-for="p in pages" :key="p.id" :to="`/page/${p.slug}`" class="card">
          <div class="card-title">{{ p.title }}</div>
          <div class="row mt-16"><span class="mono">{{ p.authorName }}</span><span class="mono">{{ fmt(p.updatedAt) }}</span></div>
        </router-link>
      </div>
      <div v-else class="empty"><p class="text-muted">暂无页面</p></div>

      <div v-if="totalPages>1" class="pagination-row">
        <button class="btn btn-ghost" :disabled="cur===0" @click="go(cur-1)">←</button>
        <span class="mono">{{ cur+1 }}/{{ totalPages }}</span>
        <button class="btn btn-ghost" :disabled="cur>=totalPages-1" @click="go(cur+1)">→</button>
      </div>
    </template>
    <div v-else class="empty"><p class="eyebrow">404</p><h2 class="section-title">分类不存在</h2></div>
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
const cat = ref<CategoryDTO|null>(null)
const parentCat = ref<CategoryDTO|null>(null)
const grandCat = ref<CategoryDTO|null>(null)
const depth = ref(1)
const pages = ref<PageDTO[]>([])
const loading = ref(true)
const cur = ref(0)
const totalPages = ref(0)

onMounted(async () => { await load(Number(route.params.id)) })
watch(() => route.params.id, async (v) => { cur.value = 0; await load(Number(v)) })

async function load(id: number) {
  loading.value = true
  try {
    const r = await getCategoryById(id)
    if (r.code !== 200) { cat.value = null; return }
    cat.value = r.data; parentCat.value = null; grandCat.value = null; depth.value = 1

    // Load parent → determine depth
    if (cat.value.parentId) {
      depth.value = 2
      const pr = await getCategoryById(cat.value.parentId)
      if (pr.code === 200) {
        parentCat.value = pr.data
        // Load grandparent
        if (parentCat.value.parentId) {
          depth.value = 3
          const gr = await getCategoryById(parentCat.value.parentId)
          if (gr.code === 200) grandCat.value = gr.data
        }
      }
    }

    // Load pages
    const pr = await getPagesByCategory(id, cur.value, 10)
    if (pr.code === 200) { pages.value = pr.data.content; totalPages.value = pr.data.totalPages }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function go(p: number) { cur.value = p; await load(cat.value!.id) }
function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN') }
</script>
