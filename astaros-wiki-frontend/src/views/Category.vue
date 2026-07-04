<template>
  <div class="cp-root">
    <div v-if="initLoading" class="loading"><div class="spinner"></div></div>
    <template v-else-if="root">
      <!-- Breadcrumb title -->
      <p class="eyebrow">分类</p>
      <h1 class="section-title">{{ breadcrumb }}</h1>
      <p v-if="root.description" class="text-muted mb-16">{{ root.description }}</p>

      <!-- L1: siblings bar -->
      <div v-if="l1Tabs.length > 1" class="cp-bar cp-bar--l1">
        <button v-for="t in l1Tabs" :key="t.id"
          class="cp-tab cp-tab--l1" :class="{ active: t.id === sel1 }"
          @click="clickL1(t)">{{ t.name }}</button>
      </div>

      <!-- L2: children bar -->
      <Transition name="cp-slide">
        <div v-if="sel1 && l2Tabs.length > 1" class="cp-bar cp-bar--l2">
          <button v-for="t in l2Tabs" :key="t.id"
            class="cp-tab cp-tab--l2" :class="{ active: t.id === sel2 }"
            @click="clickL2(t)">{{ t.name }}</button>
        </div>
      </Transition>

      <!-- L3: grandchildren bar -->
      <Transition name="cp-slide">
        <div v-if="sel2 && l3Tabs.length >= 1" class="cp-bar cp-bar--l3">
          <button v-for="t in l3Tabs" :key="t.id"
            class="cp-tab cp-tab--l3"
            @click="clickL3(t)">{{ t.name }}</button>
        </div>
      </Transition>

      <div class="divider"></div>

      <!-- Page content -->
      <div v-if="pageLoading" class="loading"><div class="spinner"></div></div>
      <template v-else-if="pages.length">
        <div class="stack">
          <router-link v-for="p in pages" :key="p.id" :to="`/page/${p.slug}`" class="card">
            <div class="card-title">{{ p.title }}</div>
            <div class="row mt-8"><span class="mono">{{ p.authorName }}</span><span class="mono">{{ fmt(p.updatedAt) }}</span></div>
          </router-link>
        </div>
        <div v-if="totalPages>1" class="pagination-row">
          <button class="btn btn-ghost" :disabled="cur===0" @click="go(cur-1)">←</button>
          <span class="mono">{{ cur+1 }}/{{ totalPages }}</span>
          <button class="btn btn-ghost" :disabled="cur>=totalPages-1" @click="go(cur+1)">→</button>
        </div>
      </template>
      <div v-else class="empty"><h2 class="section-title">暂无页面</h2></div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useCategoryStore } from '../stores/category'
import { getCategoryById } from '../api/category'
import { getPagesByCategory } from '../api/page'
import type { CategoryDTO } from '../api/category'
import type { PageDTO } from '../api/page'

const route = useRoute()
const store = useCategoryStore()

// Tree searcher
function find(id: number, nodes: CategoryDTO[]): CategoryDTO | null {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children?.length) { const f = find(id, n.children); if (f) return f }
  }
  return null
}

// State — all in-page, no router.push
const root = ref<CategoryDTO | null>(null)  // initial category from URL
const sel1 = ref<number>(0)   // L1 selected tab ID (0 = none / "全部")
const sel2 = ref<number>(0)   // L2 selected tab ID
const sel3 = ref<number>(0)   // L3 selected tab ID
const pages = ref<PageDTO[]>([])
const initLoading = ref(true)
const pageLoading = ref(false)
const cur = ref(0); const totalPages = ref(0)

// Breadcrumb: 物品 / 武器 / 剑
const breadcrumb = computed(() => {
  const parts = [root.value?.name || '']
  if (sel1.value) { const n = find(sel1.value, store.categories); if (n) parts.push(n.name) }
  if (sel2.value) { const n = find(sel2.value, store.categories); if (n) parts.push(n.name) }
  if (sel3.value) { const n = find(sel3.value, store.categories); if (n) parts.push(n.name) }
  return parts.join(' / ')
})

// L1: root's children (if root has children) OR root's siblings (if root is a leaf)
const l1Tabs = computed((): CategoryDTO[] => {
  if (!root.value) return []
  const r = find(root.value.id, store.categories)
  if (!r) return []
  const all = [...(r.children || [])]
  if (all.length === 0) {
    // leaf — show siblings
    const p = root.value.parentId ? find(root.value.parentId, store.categories) : null
    const sibs = p?.children || store.categories
    if (sibs.length <= 1) return []
    return [{ id: root.value.parentId || root.value.id, name: '全部' } as CategoryDTO, ...sibs.filter((s: CategoryDTO) => s.id !== root.value!.id)]
  }
  return [{ id: root.value.id, name: '全部' } as CategoryDTO, ...all]
})

// L2: children of selected L1
const l2Tabs = computed((): CategoryDTO[] => {
  if (!sel1.value) return []
  const n = find(sel1.value, store.categories)
  if (!n?.children?.length) return []
  return [{ id: n.id, name: '全部' } as CategoryDTO, ...n.children]
})

// L3: children of selected L2 (always show bar if sel2)
const l3Tabs = computed((): CategoryDTO[] => {
  if (!sel2.value) return []
  const n = find(sel2.value, store.categories)
  if (!n) return []
  return [{ id: n.id, name: '全部' } as CategoryDTO, ...(n.children || [])]
})

// Determine which category ID to load pages for
const activeCatId = computed(() => sel3.value || sel2.value || sel1.value || root.value?.id || 0)

onMounted(async () => {
  await store.fetchCategories()
  await loadInitial(Number(route.params.id))
})
watch(() => route.params.id, async (v) => {
  sel1.value = 0; sel2.value = 0; sel3.value = 0
  await loadInitial(Number(v))
})

async function loadInitial(id: number) {
  initLoading.value = true
  try {
    const r = await getCategoryById(id)
    if (r.code === 200) root.value = r.data
    await loadPages(activeCatId.value)
  } catch (e) { console.error(e) } finally { initLoading.value = false }
}

async function loadPages(catId: number) {
  pageLoading.value = true
  try {
    const r = await getPagesByCategory(catId, cur.value, 10)
    if (r.code === 200) { pages.value = r.data.content; totalPages.value = r.data.totalPages }
  } catch (e) { console.error(e) } finally { pageLoading.value = false }
}

// Click handlers — no router.push!
function clickL1(t: CategoryDTO) {
  if (sel1.value === t.id) { sel1.value = 0; sel2.value = 0; sel3.value = 0 }  // deselect
  else { sel1.value = t.id; sel2.value = 0; sel3.value = 0 }  // select
  pageload()
}
function clickL2(t: CategoryDTO) {
  if (sel2.value === t.id) { sel2.value = 0; sel3.value = 0 }  // deselect
  else { sel2.value = t.id; sel3.value = 0 }  // select
  pageload()
}
function clickL3(t: CategoryDTO) {
  sel3.value = sel3.value === t.id ? 0 : t.id
  pageload()
}
async function pageload() { cur.value = 0; await loadPages(activeCatId.value) }
async function go(p: number) { cur.value = p; await loadPages(activeCatId.value) }
function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN') }
</script>

<style scoped>
.cp-root { max-width: 1400px; }

/* Bars */
.cp-bar { display: flex; width: 100%; gap: 0; padding: 0; overflow-x: auto; -webkit-overflow-scrolling: touch; }
.cp-bar--l1 { border-bottom: 2px solid var(--border); }
.cp-bar--l2 { padding-top: 8px; border-bottom: 1px solid color-mix(in srgb, var(--fg), 6%); }
.cp-bar--l3 { padding-top: 4px; }

/* Tabs — all flex:1 equal width, min-height 44px */
.cp-tab {
  flex: 1; text-align: center; min-width: 0; min-height: 44px;
  font-family: var(--mono); text-transform: uppercase; letter-spacing: .06em;
  background: none; cursor: pointer; border: none;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  transition: all .15s;
}
.cp-tab:active { transform: scale(.97); }

.cp-tab--l1 {
  padding: 12px 4px; font-size: 12px; font-weight: 600; color: var(--muted);
  border-bottom: 2px solid transparent; margin-bottom: -2px;
}
.cp-tab--l1:hover { color: var(--fg); }
.cp-tab--l1.active { color: var(--fg); border-bottom-color: var(--fg); }

.cp-tab--l2 {
  padding: 10px 4px; font-size: 11px; font-weight: 500; color: var(--muted);
  border: 1px solid var(--border); margin: 2px;
}
.cp-tab--l2:hover { color: var(--fg); border-color: var(--muted); transform: translateY(-1px); }
.cp-tab--l2.active { color: var(--fg); border-color: var(--fg); }
[data-theme='dark'] .cp-tab--l2.active { background: color-mix(in srgb, var(--fg), 10%); }
[data-theme='light'] .cp-tab--l2.active { background: rgba(0,0,0,.05); }

.cp-tab--l3 {
  padding: 8px 4px; font-size: 10px; color: var(--muted);
  border: 1px solid color-mix(in srgb, var(--fg), 8%); margin: 1px;
  background: color-mix(in srgb, var(--fg), 2%);
}
.cp-tab--l3:hover { color: var(--fg); border-color: var(--fg); transform: translateY(-1px); }

/* Slide animation */
.cp-slide-enter-active { transition: all .2s ease-out; overflow: hidden; }
.cp-slide-leave-active { transition: all .12s ease-in; overflow: hidden; }
.cp-slide-enter-from, .cp-slide-leave-to { opacity: 0; max-height: 0; }
.cp-slide-enter-to, .cp-slide-leave-from { max-height: 80px; }
</style>
