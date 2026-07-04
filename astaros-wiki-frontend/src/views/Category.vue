<template>
  <div class="cat-page">
    <div v-if="loading" class="loading"><div class="spinner"></div></div>
    <template v-else-if="display">
      <p class="eyebrow">分类</p>
      <h1 class="section-title">{{ display.name }}</h1>
      <p v-if="display.description" class="text-muted mb-24">{{ display.description }}</p>

      <!-- Level 1 tabs: full-width bar -->
      <div v-if="l1Tabs.length > 1" class="cat-bar cat-bar--l1">
        <button v-for="t in l1Tabs" :key="t.id"
          class="cat-tab cat-tab--l1" :class="{ active: t.id === selectedL1 }"
          @click="selectL1(t)">{{ t.name }}</button>
      </div>

      <!-- Level 2 tabs: same full width, slides in -->
      <Transition name="cat-in">
        <div v-if="l2Tabs.length > 1" class="cat-bar cat-bar--l2">
          <button v-for="t in l2Tabs" :key="t.id"
            class="cat-tab cat-tab--l2" :class="{ active: t.id === selectedL2 }"
            @click="selectL2(t)">{{ t.name }}</button>
        </div>
      </Transition>

      <!-- Level 3 tabs -->
      <Transition name="cat-in">
        <div v-if="l3Tabs.length > 1" class="cat-bar cat-bar--l3">
          <button v-for="t in l3Tabs" :key="t.id"
            class="cat-tab cat-tab--l3"
            @click="router.push(`/category/${t.id}`)">{{ t.name }}</button>
        </div>
      </Transition>

      <div class="divider"></div>

      <!-- Page list -->
      <div v-if="pages.length" class="stack">
        <router-link v-for="p in pages" :key="p.id" :to="`/page/${p.slug}`" class="card">
          <div class="card-title">{{ p.title }}</div>
          <div class="row mt-8"><span class="mono">{{ p.authorName }}</span><span class="mono">{{ fmt(p.updatedAt) }}</span></div>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCategoryStore } from '../stores/category'
import { getCategoryById } from '../api/category'
import { getPagesByCategory } from '../api/page'
import type { CategoryDTO } from '../api/category'
import type { PageDTO } from '../api/page'

const route = useRoute(); const router = useRouter()
const categoryStore = useCategoryStore()
const cat = ref<CategoryDTO|null>(null); const parent = ref<CategoryDTO|null>(null)
const pages = ref<PageDTO[]>([]); const loading = ref(true)
const cur = ref(0); const totalPages = ref(0)
const selectedL1 = ref<number>(0)
const selectedL2 = ref<number>(0)

const display = computed(() => parent.value || cat.value)

// Recursive tree search
function findInTree(id: number, nodes: CategoryDTO[]): CategoryDTO | null {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children?.length) { const f = findInTree(id, n.children); if (f) return f }
  }
  return null
}

// L1: siblings (or parent's children if leaf) + "全部" as first
const l1Tabs = computed((): CategoryDTO[] => {
  const tree = categoryStore.categories
  const node = findInTree(cat.value?.id || 0, tree)
  if (!node) return []
  // Find parent in tree
  const p = node.parentId ? findInTree(node.parentId, tree) : null
  const siblings = p?.children || tree // parent's children, or root-level if no parent
  if (siblings.length <= 1) return []
  const all: CategoryDTO = { id: node.id, name: '全部', parentId: node.parentId } as CategoryDTO
  return [all, ...siblings.filter((s: CategoryDTO) => s.id !== node.id)]
})

// L2: children of selected L1
const l2Tabs = computed((): CategoryDTO[] => {
  if (!selectedL1.value) return []
  const node = findInTree(selectedL1.value, categoryStore.categories)
  if (!node?.children?.length) return []
  const all: CategoryDTO = { id: node.id, name: '全部', parentId: node.parentId } as CategoryDTO
  return [all, ...node.children]
})

// L3: children of selected L2
const l3Tabs = computed((): CategoryDTO[] => {
  if (!selectedL2.value) return []
  const node = findInTree(selectedL2.value, categoryStore.categories)
  if (!node?.children?.length) return []
  const all: CategoryDTO = { id: node.id, name: '全部', parentId: node.parentId } as CategoryDTO
  return [all, ...node.children]
})

onMounted(async () => { await categoryStore.fetchCategories(); await load(Number(route.params.id)) })
watch(() => route.params.id, async (v) => { cur.value = 0; await load(Number(v)) })

async function load(id: number) {
  loading.value = true
  try {
    const r = await getCategoryById(id)
    if (r.code===200) { cat.value = r.data; parent.value = r.data.parentId ? (await getCategoryById(r.data.parentId)).data : null }
    const pr = await getPagesByCategory(id, cur.value, 10)
    if (pr.code===200) { pages.value = pr.data.content; totalPages.value = pr.data.totalPages }
    selectedL1.value = id; selectedL2.value = 0
  } catch(e) { console.error(e) } finally { loading.value = false }
}

function selectL1(t: CategoryDTO) { selectedL1.value = t.id; selectedL2.value = 0; router.push(`/category/${t.id}`) }
function selectL2(t: CategoryDTO) { selectedL2.value = t.id; router.push(`/category/${t.id}`) }
async function go(p: number) { cur.value = p; await load(Number(route.params.id)) }
function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN') }
</script>

<style scoped>
.cat-page { max-width: 1400px; }

/* Bars — all same full width */
.cat-bar { display: flex; flex-wrap: wrap; gap: 0; width: 100%; padding: 8px 0; }
.cat-bar--l1 { border-bottom: 2px solid var(--border); }
.cat-bar--l2 { border-bottom: 1px solid color-mix(in srgb, var(--fg) 5%, transparent); }
.cat-bar--l3 { padding: 4px 0; }

/* Tabs */
.cat-tab {
  font-family: var(--mono); text-transform: uppercase; letter-spacing: .06em;
  background: none; cursor: pointer; border: none; white-space: nowrap; transition: all .15s;
}
.cat-tab--l1 {
  flex: 1; padding: 10px 16px; font-size: 12px; font-weight: 600;
  color: var(--muted); border-bottom: 2px solid transparent; margin-bottom: -2px;
}
.cat-tab--l1:hover { color: var(--fg); }
.cat-tab--l1.active { color: var(--fg); border-bottom-color: var(--fg); }

.cat-tab--l2 {
  flex: 1; padding: 8px 12px; font-size: 11px; font-weight: 500;
  color: var(--muted); border: 1px solid var(--border); margin: 2px;
}
.cat-tab--l2:hover { color: var(--fg); border-color: var(--muted); transform: translateY(-1px); }
.cat-tab--l2.active { color: var(--fg); border-color: var(--fg); }
[data-theme='dark'] .cat-tab--l2.active { background: color-mix(in srgb, var(--fg) 12%, transparent); }
[data-theme='light'] .cat-tab--l2.active { background: color-mix(in srgb, var(--fg) 8%, transparent); }

.cat-tab--l3 {
  flex: 1; padding: 6px 10px; font-size: 10px;
  color: var(--muted); border: 1px solid color-mix(in srgb, var(--fg) 6%, transparent); margin: 2px;
  background: color-mix(in srgb, var(--fg) 2%, transparent);
}
.cat-tab--l3:hover { color: var(--fg); border-color: var(--fg); transform: translateY(-1px); }

/* Animations */
.cat-in-enter-active { transition: all .2s ease-out; overflow: hidden; }
.cat-in-leave-active { transition: all .12s ease-in; overflow: hidden; }
.cat-in-enter-from, .cat-in-leave-to { opacity: 0; max-height: 0; }
.cat-in-enter-to, .cat-in-leave-from { max-height: 80px; }
</style>
