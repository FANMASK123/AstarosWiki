<template>
  <div class="cp-root">
    <div v-if="loading" class="loading"><div class="spinner"></div></div>
    <template v-else-if="display">
      <p class="eyebrow">分类</p>
      <h1 class="section-title">{{ display.name }}</h1>
      <p v-if="display.description" class="text-muted mb-16">{{ display.description }}</p>

      <!-- L1: siblings bar, equal-width tabs -->
      <div v-if="l1Tabs.length > 1" class="cp-bar cp-bar--l1">
        <button v-for="t in l1Tabs" :key="t.id"
          class="cp-tab cp-tab--l1" :class="{ active: t.id === currentId }"
          @click="goTab(t)">{{ t.name }}</button>
      </div>

      <!-- L2: children bar, equal-width, slides down -->
      <Transition name="cp-slide">
        <div v-if="l2Tabs.length > 1" class="cp-bar cp-bar--l2">
          <button v-for="t in l2Tabs" :key="t.id"
            class="cp-tab cp-tab--l2" :class="{ active: t.id === l2Active }"
            @click="goL2(t)">{{ t.name }}</button>
        </div>
      </Transition>

      <!-- L3: grandchildren bar, equal-width -->
      <Transition name="cp-slide">
        <div v-if="l3Tabs.length >= 1" class="cp-bar cp-bar--l3">
          <button v-for="t in l3Tabs" :key="t.id"
            class="cp-tab cp-tab--l3"
            @click="goTab(t)">{{ t.name }}</button>
        </div>
      </Transition>

      <div class="divider"></div>

      <div v-if="loading" class="loading"><div class="spinner"></div></div>
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
const store = useCategoryStore()
const cat = ref<CategoryDTO|null>(null)
const parent = ref<CategoryDTO|null>(null)
const pages = ref<PageDTO[]>([])
const loading = ref(true)
const cur = ref(0); const totalPages = ref(0)
const l2Active = ref<number>(0) // which L2 tab is active (for L3)

// find in store tree
function find(id: number, nodes: CategoryDTO[]): CategoryDTO|null {
  for (const n of nodes) {
    if (n.id===id) return n
    if (n.children?.length) { const f=find(id,n.children); if(f)return f }
  }
  return null
}

const display = computed(() => parent.value||cat.value)
const currentId = computed(() => cat.value?.id||0)

// L1: siblings of current category (or parent's children)
const l1Tabs = computed((): CategoryDTO[] => {
  if (!cat.value) return []
  // Find parent in tree
  const p = cat.value.parentId ? find(cat.value.parentId, store.categories) : null
  const sibs = p?.children || store.categories
  if (sibs.length <= 1) return []
  // "全部" goes back to parent (or stays on root if no parent)
  const parentId = cat.value.parentId || cat.value.id
  return [{id: parentId, name:'全部'} as CategoryDTO, ...sibs.filter((s:CategoryDTO)=>s.id!==cat.value!.id)]
})

// L2: children of current category
const l2Tabs = computed((): CategoryDTO[] => {
  if (!cat.value) return []
  const node = find(cat.value.id, store.categories)
  if (!node?.children?.length) return []
  return [{id:node.id,name:'全部'} as CategoryDTO, ...node.children]
})

// L3: children of selected L2 (always show "全部" bar if L2 is active)
const l3Tabs = computed((): CategoryDTO[] => {
  if (!l2Active.value) return []
  const node = find(l2Active.value, store.categories)
  if (!node) return []
  const children = node.children || []
  return [{id: node.id, name:'全部'} as CategoryDTO, ...children]
})

onMounted(async ()=>{ await store.fetchCategories(); await load(Number(route.params.id)) })
watch(()=>route.params.id, async (v)=>{ cur.value=0; await load(Number(v)) })

async function load(id:number){
  loading.value=true; l2Active.value=0
  try{
    const r=await getCategoryById(id)
    if(r.code===200){ cat.value=r.data; parent.value=r.data.parentId?(await getCategoryById(r.data.parentId)).data:null }
    const pr=await getPagesByCategory(id,cur.value,10)
    if(pr.code===200){ pages.value=pr.data.content; totalPages.value=pr.data.totalPages }
  }catch(e){console.error(e)}finally{loading.value=false}
}

function goTab(t:CategoryDTO){ router.push(`/category/${t.id}`) }
function goL2(t:CategoryDTO){ l2Active.value=l2Active.value===t.id?0:t.id }
async function go(p:number){ cur.value=p; await load(Number(route.params.id)) }
function fmt(d:string){return new Date(d).toLocaleDateString('zh-CN')}
</script>

<style scoped>
.cp-root{max-width:1400px}

/* Bars */
.cp-bar{display:flex;width:100%;gap:0;padding:0;overflow-x:auto;-webkit-overflow-scrolling:touch}
.cp-bar--l1{border-bottom:2px solid var(--border)}
.cp-bar--l2{border-bottom:1px solid color-mix(in srgb,var(--fg),6%);padding-top:8px}
.cp-bar--l3{padding-top:4px}

/* Tabs — all flex:1 equal width */
.cp-tab{flex:1;text-align:center;font-family:var(--mono);text-transform:uppercase;letter-spacing:.06em;background:none;cursor:pointer;border:none;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;transition:all .15s}

.cp-tab--l1{padding:10px 4px;font-size:12px;font-weight:600;color:var(--muted);border-bottom:2px solid transparent;margin-bottom:-2px}
.cp-tab--l1:hover{color:var(--fg)}
.cp-tab--l1.active{color:var(--fg);border-bottom-color:var(--fg)}

.cp-tab--l2{padding:8px 4px;font-size:11px;font-weight:500;color:var(--muted);border:1px solid var(--border);margin:2px}
.cp-tab--l2:hover{color:var(--fg);border-color:var(--muted);transform:translateY(-1px)}
.cp-tab--l2.active{color:var(--fg);border-color:var(--fg)}
[data-theme='dark'] .cp-tab--l2.active{background:color-mix(in srgb,var(--fg),12%)}
[data-theme='light'] .cp-tab--l2.active{background:color-mix(in srgb,var(--fg),8%)}

.cp-tab--l3{padding:6px 4px;font-size:10px;color:var(--muted);border:1px solid color-mix(in srgb,var(--fg),8%);margin:1px;background:color-mix(in srgb,var(--fg),2%)}
.cp-tab--l3:hover{color:var(--fg);border-color:var(--fg);transform:translateY(-1px)}

/* Slide animation */
.cp-slide-enter-active{transition:all .2s ease-out;overflow:hidden}
.cp-slide-leave-active{transition:all .12s ease-in;overflow:hidden}
.cp-slide-enter-from,.cp-slide-leave-to{opacity:0;max-height:0}
.cp-slide-enter-to,.cp-slide-leave-from{max-height:80px}
</style>
