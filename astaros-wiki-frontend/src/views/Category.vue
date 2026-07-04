<template>
  <div class="cat-page">
    <div v-if="loading" class="loading"><div class="spinner"></div></div>
    <template v-else-if="display">
      <p class="eyebrow">分类</p>
      <h1 class="section-title">{{ display.name }}</h1>
      <p v-if="display.description" class="text-muted" style="margin-bottom:20px">{{ display.description }}</p>

      <!-- Three-level horizontal tab navigation -->
      <div v-if="hasTabs" class="cat-tabs">
        <!-- Level 1 tabs -->
        <div class="cat-l1">
          <button
            v-for="t in l1Tabs"
            :key="t.id"
            class="cat-btn cat-btn--l1"
            :class="{ active: selectedL1 === t.id }"
            @click="selectL1(t)"
          >{{ t.name }}</button>
        </div>

        <!-- Level 2 tabs (below selected L1) -->
        <Transition name="cat-expand">
          <div v-if="selectedL1 && l2Tabs.length" class="cat-l2">
            <button
              v-for="t in l2Tabs"
              :key="t.id"
              class="cat-btn cat-btn--l2"
              :class="{ active: selectedL2 === t.id }"
              @click="selectL2(t)"
            >{{ t.name }}</button>
          </div>
        </Transition>

        <!-- Level 3 tabs (below selected L2) -->
        <Transition name="cat-expand">
          <div v-if="selectedL2 && l3Tabs.length" class="cat-l3">
            <button
              v-for="t in l3Tabs"
              :key="t.id"
              class="cat-btn cat-btn--l3"
              @click="router.push(`/category/${t.id}`)"
            >{{ t.name }}</button>
          </div>
        </Transition>
      </div>

      <!-- Page list -->
      <div class="divider"></div>
      <div v-if="pages.length" class="stack">
        <router-link v-for="p in pages" :key="p.id" :to="`/page/${p.slug}`" class="card">
          <div class="card-title">{{ p.title }}</div>
          <div class="row mt-8">
            <span class="mono">{{ p.authorName }}</span>
            <span class="mono">{{ fmt(p.updatedAt) }}</span>
          </div>
        </router-link>
      </div>
      <div v-else class="empty">
        <h2 class="section-title">暂无页面</h2>
        <p class="text-muted">该分类下暂未发布页面</p>
      </div>

      <!-- Pagination -->
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

// Recursively find a category in the tree by ID
function findInTree(id: number, nodes: CategoryDTO[]): CategoryDTO | null {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children?.length) {
      const found = findInTree(id, n.children)
      if (found) return found
    }
  }
  return null
}

const display = computed(() => parent.value || cat.value)
const hasTabs = computed(() => l1Tabs.value.length > 1)

// L1 tabs: siblings of the current category (or top-level if none)
const l1Tabs = computed((): CategoryDTO[] => {
  const all = parent.value?.children || cat.value?.children || []
  if (all.length === 0 && parent.value) {
    // Show parent's children as L1
    return parent.value.children || []
  }
  // If we're viewing a child with no children, show its parent
  if (cat.value?.parentId && all.length === 0 && parent.value?.children) {
    return parent.value.children
  }
  // Add "全部" header
  const current = parent.value || cat.value
  if (current && all.length > 0) {
    return [{ id: current.id, name: '全部', parentId: current.parentId } as CategoryDTO, ...all]
  }
  return all
})

const l2Tabs = computed((): CategoryDTO[] => {
  if (!selectedL1.value) return []
  const tree = categoryStore.categories
  const target = findInTree(selectedL1.value, tree)
  if (!target?.children?.length) return []
  return [{ id: target.id, name: '全部', parentId: target.parentId } as CategoryDTO, ...target.children]
})

const l3Tabs = computed((): CategoryDTO[] => {
  if (!selectedL2.value) return []
  const tree = categoryStore.categories
  const target = findInTree(selectedL2.value, tree)
  return target?.children || []
})

onMounted(async () => { await categoryStore.fetchCategories(); await load(Number(route.params.id)) })
watch(() => route.params.id, async (v) => { cur.value = 0; await load(Number(v)) })

async function load(id: number) {
  loading.value = true
  try {
    const r = await getCategoryById(id); if (r.code===200) { cat.value = r.data; parent.value = null; if (cat.value.parentId) { const pr = await getCategoryById(cat.value.parentId); if (pr.code===200) parent.value = pr.data } }
    const pr = await getPagesByCategory(id, cur.value, 10); if (pr.code===200) { pages.value = pr.data.content; totalPages.value = pr.data.totalPages }
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

/* Tab containers */
.cat-tabs { margin-bottom: 8px; }
.cat-l1 { display: flex; flex-wrap: wrap; gap: 0; border-bottom: 2px solid var(--border); }
.cat-l2 { display: flex; flex-wrap: wrap; gap: 4px; padding: 8px 0; border-bottom: 1px solid color-mix(in srgb, var(--fg) 6%, transparent); }
.cat-l3 { display: flex; flex-wrap: wrap; gap: 4px; padding: 6px 0; }

/* Tab buttons */
.cat-btn {
  font-family: var(--mono); text-transform: uppercase; letter-spacing: .06em;
  background: none; cursor: pointer; transition: all .15s; border: none; white-space: nowrap;
}
.cat-btn--l1 {
  flex: 0 0 auto; padding: 8px 16px; font-size: 11px; font-weight: 600;
  color: var(--muted); border-bottom: 2px solid transparent; margin-bottom: -2px;
}
.cat-btn--l1:hover { color: var(--fg); }
.cat-btn--l1.active { color: var(--fg); border-bottom-color: var(--fg); }

.cat-btn--l2 {
  padding: 5px 12px; font-size: 10px; font-weight: 500;
  color: var(--muted); border: 1px solid var(--border);
}
.cat-btn--l2:hover { color: var(--fg); border-color: var(--muted); transform: translateY(-1px); }
.cat-btn--l2.active { color: var(--fg); border-color: var(--fg); background: color-mix(in srgb, var(--fg) 8%, transparent); }
[data-theme='dark'] .cat-btn--l2.active { background: color-mix(in srgb, var(--fg) 12%, transparent); }
[data-theme='light'] .cat-btn--l2.active { background: color-mix(in srgb, var(--fg) 8%, transparent); }

.cat-btn--l3 {
  padding: 4px 10px; font-size: 10px;
  color: var(--muted); border: 1px solid color-mix(in srgb, var(--fg) 8%, transparent);
  background: color-mix(in srgb, var(--fg) 2%, transparent);
}
.cat-btn--l3:hover { color: var(--fg); border-color: var(--fg); transform: translateY(-1px); }

/* Expand transition */
.cat-expand-enter-active { transition: all .2s ease-out; overflow: hidden; }
.cat-expand-leave-active { transition: all .12s ease-in; overflow: hidden; }
.cat-expand-enter-from, .cat-expand-leave-to { opacity: 0; max-height: 0; }
.cat-expand-enter-to, .cat-expand-leave-from { max-height: 120px; }
</style>
