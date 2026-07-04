<template>
  <div class="cat-page">
    <div v-if="loading" class="loading"><div class="spinner"></div></div>
    <template v-else-if="display">
      <p class="eyebrow">分类</p>
      <h1 class="section-title">{{ display.name }}</h1>
      <p v-if="display.description" class="text-muted mb-24">{{ display.description }}</p>

      <!-- L1 tabs: fixed-width, wrap -->
      <div v-if="l1Tabs.length > 1" class="cat-bar cat-bar--l1">
        <button v-for="t in l1Tabs" :key="t.id"
          class="cat-tab cat-tab--l1" :class="{ active: t.id === selectedL1 }"
          @click="selectL1(t)">{{ t.name }}</button>
      </div>

      <!-- L2 tabs -->
      <Transition name="cat-in">
        <div v-if="l2Tabs.length > 1" class="cat-bar cat-bar--l2">
          <button v-for="t in l2Tabs" :key="t.id"
            class="cat-tab cat-tab--l2" :class="{ active: t.id === selectedL2 }"
            @click="selectL2(t)">{{ t.name }}</button>
        </div>
      </Transition>

      <!-- L3 tabs -->
      <Transition name="cat-in">
        <div v-if="l3Tabs.length > 1" class="cat-bar cat-bar--l3">
          <button v-for="t in l3Tabs" :key="t.id"
            class="cat-tab cat-tab--l3"
            @click="selectL3(t)">{{ t.name }}</button>
        </div>
      </Transition>

      <div class="divider"></div>

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
const selectedL3 = ref<number>(0)

const display = computed(() => parent.value || cat.value)
const effectiveId = computed(() => selectedL3.value || selectedL2.value || selectedL1.value || cat.value?.id || 0)

function findInTree(id: number, nodes: CategoryDTO[]): CategoryDTO | null {
  for (const n of nodes) { if (n.id === id) return n; if (n.children?.length) { const f = findInTree(id, n.children); if (f) return f } }
  return null
}

// L1: siblings of current node, no duplicates
const l1Tabs = computed((): CategoryDTO[] => {
  const tree = categoryStore.categories
  const node = findInTree(cat.value?.id || 0, tree)
  if (!node) return []
  const p = node.parentId ? findInTree(node.parentId, tree) : null
  const siblings = (p?.children || tree).filter((s: CategoryDTO) => s.id !== node.id)
  if (siblings.length === 0) return []
  return [{ id: node.id, name: '全部' } as CategoryDTO, ...siblings]
})

// L2: children of selected L1
const l2Tabs = computed((): CategoryDTO[] => {
  if (!selectedL1.value || selectedL1.value === cat.value?.id) return []
  const node = findInTree(selectedL1.value, categoryStore.categories)
  if (!node?.children?.length) return []
  return [{ id: node.id, name: '全部(' + node.name + ')' } as CategoryDTO, ...node.children]
})

// L3: children of selected L2
const l3Tabs = computed((): CategoryDTO[] => {
  if (!selectedL2.value || selectedL2.value === selectedL1.value) return []
  const node = findInTree(selectedL2.value, categoryStore.categories)
  if (!node?.children?.length) return []
  return [{ id: node.id, name: '全部(' + node.name + ')' } as CategoryDTO, ...node.children]
})

onMounted(async () => { await categoryStore.fetchCategories(); await load(Number(route.params.id)) })
watch(() => route.params.id, async (v) => { reset(); await load(Number(v)) })

function reset() { cur.value = 0; selectedL1.value = 0; selectedL2.value = 0; selectedL3.value = 0 }

async function load(id: number) {
  loading.value = true
  try {
    const r = await getCategoryById(id)
    if (r.code===200) { cat.value = r.data; parent.value = r.data.parentId ? (await getCategoryById(r.data.parentId)).data : null }
    selectedL1.value = id
    await fetchPages()
  } catch(e) { console.error(e) } finally { loading.value = false }
}

async function fetchPages() {
  const pr = await getPagesByCategory(effectiveId.value, cur.value, 10)
  if (pr.code===200) { pages.value = pr.data.content; totalPages.value = pr.data.totalPages }
}

function selectL1(t: CategoryDTO) {
  selectedL1.value = t.id; selectedL2.value = 0; selectedL3.value = 0
  router.replace(`/category/${t.id}`)
  fetchPages()
}
function selectL2(t: CategoryDTO) {
  selectedL2.value = t.id; selectedL3.value = 0
  router.replace(`/category/${t.id}`)
  fetchPages()
}
function selectL3(t: CategoryDTO) {
  selectedL3.value = t.id
  router.replace(`/category/${t.id}`)
  fetchPages()
}
async function go(p: number) { cur.value = p; await fetchPages() }
function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN') }
</script>

<style scoped>
.cat-page { max-width: 1400px; }

.cat-bar { display: flex; flex-wrap: wrap; gap: 4px; padding: 6px 0; }
.cat-bar--l1 { border-bottom: 2px solid var(--border); }
.cat-bar--l2 { border-bottom: 1px solid color-mix(in srgb, var(--fg) 5%, transparent); }
.cat-bar--l3 { }

.cat-tab {
  min-width: 120px; padding: 8px 16px; font-family: var(--mono);
  text-transform: uppercase; letter-spacing: .06em; font-size: 11px; font-weight: 600;
  background: none; cursor: pointer; border: none; white-space: nowrap;
  text-align: center; color: var(--muted); transition: all .15s;
  border-bottom: 2px solid transparent; margin-bottom: -2px;
}
.cat-tab:hover { color: var(--fg); }
.cat-tab.active { color: var(--fg); border-bottom-color: var(--fg); }

.cat-tab--l2, .cat-tab--l3 {
  font-weight: 500; font-size: 10px; padding: 6px 12px; min-width: 100px;
  border: 1px solid var(--border); border-bottom: 1px solid var(--border);
  margin-bottom: 0;
}
.cat-tab--l2:hover, .cat-tab--l3:hover { border-color: var(--muted); transform: translateY(-1px); }
.cat-tab--l2.active, .cat-tab--l3.active { color: var(--fg); border-color: var(--fg); background: color-mix(in srgb, var(--fg) 8%, transparent); }
[data-theme='dark'] .cat-tab--l2.active, [data-theme='dark'] .cat-tab--l3.active { background: color-mix(in srgb, var(--fg) 14%, transparent); }

.cat-in-enter-active { transition: all .2s ease-out; overflow: hidden; }
.cat-in-leave-active { transition: all .12s ease-in; overflow: hidden; }
.cat-in-enter-from, .cat-in-leave-to { opacity: 0; max-height: 0; }
.cat-in-enter-to, .cat-in-leave-from { max-height: 80px; }
</style>
