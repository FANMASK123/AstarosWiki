<template>
  <div class="cp-root">
    <div v-if="loading" class="loading"><div class="spinner"></div></div>
    <template v-else-if="display">
      <p class="eyebrow">分类</p>
      <h1 class="section-title">{{ display.name }}</h1>
      <p v-if="display.description" class="text-muted mb-16">{{ display.description }}</p>

      <!-- L1: fixed-width siblings bar, wraps if overflow -->
      <div v-if="l1Tabs.length > 1" class="cp-bar cp-bar--l1">
        <button v-for="t in l1Tabs" :key="'l1-'+t.id"
          class="cp-tab cp-tab--l1" :class="{ active: t.id === currentId }"
          @click="goTab(t)">{{ t.name }}</button>
      </div>

      <!-- L2: children bar, slides down below L1 -->
      <Transition name="cp-slide">
        <div v-if="l2Tabs.length > 1" class="cp-bar cp-bar--l2">
          <button v-for="t in l2Tabs" :key="'l2-'+t.id"
            class="cp-tab cp-tab--l2" :class="{ active: t.id === l2Active }"
            @click="goL2(t)">{{ t.name }}</button>
        </div>
      </Transition>

      <!-- L3: grandchildren bar, always shows if L2 selected -->
      <Transition name="cp-slide">
        <div v-if="l3Tabs.length >= 1" class="cp-bar cp-bar--l3">
          <button v-for="t in l3Tabs" :key="'l3-'+t.id"
            class="cp-tab cp-tab--l3" :class="{ active: t.id === l3Active }"
            @click="goL3(t)">{{ t.name }}</button>
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
      <div v-else class="empty"><h2 class="section-title">暂无页面</h2></div>
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
const l2Active = ref<number>(0)
const l3Active = ref<number>(0)

// Tree search
function find(id: number, nodes: CategoryDTO[]): CategoryDTO|null {
  for (const n of nodes) {
    if (n.id===id) return n
    if (n.children?.length) { const f=find(id,n.children); if(f)return f }
  }
  return null
}

// Current category depth (1-indexed)
const depth = computed(() => {
  if (!cat.value) return 1
  let d=1; let pid=cat.value.parentId
  while(pid){ const p=find(pid,store.categories); if(!p)break; d++; pid=p.parentId }
  return d
})

const display = computed(() => parent.value||cat.value)
const currentId = computed(() => cat.value?.id||0)

// L1: siblings
const l1Tabs = computed((): CategoryDTO[] => {
  if (!cat.value) return []
  const p = cat.value.parentId ? find(cat.value.parentId, store.categories) : null
  const sibs = p?.children || store.categories
  if (sibs.length <= 1) return []
  const parentId = cat.value.parentId || cat.value.id
  return [{id: parentId, name:'全部'} as CategoryDTO, ...sibs.filter((s:CategoryDTO)=>s.id!==cat.value!.id)]
})

// L2: children (only if depth < 3 to prevent L4)
const l2Tabs = computed((): CategoryDTO[] => {
  if (!cat.value || depth.value >= 3) return []
  const node = find(cat.value.id, store.categories)
  if (!node?.children?.length) return []
  return [{id:node.id,name:'全部'} as CategoryDTO, ...node.children]
})

// L3: selected L2's children
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
  loading.value=true; l2Active.value=0; l3Active.value=0
  try{
    const r=await getCategoryById(id)
    if(r.code===200){ cat.value=r.data; parent.value=r.data.parentId?(await getCategoryById(r.data.parentId)).data:null }
    const pr=await getPagesByCategory(id,cur.value,10)
    if(pr.code===200){ pages.value=pr.data.content; totalPages.value=pr.data.totalPages }
  }catch(e){console.error(e)}finally{loading.value=false}
}

function goTab(t:CategoryDTO){ if(t.id===currentId.value) return; l2Active.value=0; l3Active.value=0; router.push(`/category/${t.id}`) }
function goL2(t:CategoryDTO){ l2Active.value=l2Active.value===t.id?0:t.id; if(l2Active.value===0) l3Active.value=0 }
function goL3(t:CategoryDTO){ router.push(`/category/${t.id}`) }
async function go(p:number){ cur.value=p; await load(Number(route.params.id)) }
function fmt(d:string){return new Date(d).toLocaleDateString('zh-CN')}
</script>

<style scoped>
.cp-root{max-width:1400px}

/* Bars */
.cp-bar{display:flex;flex-wrap:wrap;width:100%;gap:4px;padding:0}
.cp-bar--l1{border-bottom:2px solid var(--border);padding-bottom:4px}
.cp-bar--l2{border-bottom:1px solid color-mix(in srgb,var(--fg),6%);padding-top:8px;padding-bottom:4px}
.cp-bar--l3{padding-top:4px}

/* Tabs — fixed 160px, wrap */
.cp-tab{width:160px;height:36px;flex:none;text-align:center;font-family:var(--mono);text-transform:uppercase;letter-spacing:.06em;background:none;cursor:pointer;border:none;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;transition:all .15s;display:inline-flex;align-items:center;justify-content:center}

.cp-tab--l1{font-size:12px;font-weight:600;color:var(--muted);border-bottom:2px solid transparent;margin-bottom:-2px}
.cp-tab--l1:hover{color:var(--fg)}
.cp-tab--l1.active{color:var(--fg);border-bottom-color:var(--fg)}

.cp-tab--l2{font-size:11px;font-weight:500;color:var(--muted);border:1px solid var(--border)}
.cp-tab--l2:hover{color:var(--fg);border-color:var(--muted);transform:translateY(-1px)}
.cp-tab--l2.active{color:var(--fg);border-color:var(--fg)}
[data-theme='dark'] .cp-tab--l2.active{background:color-mix(in srgb,var(--fg),12%)}
[data-theme='light'] .cp-tab--l2.active{background:color-mix(in srgb,var(--fg),8%)}

.cp-tab--l3{font-size:10px;color:var(--muted);border:1px solid color-mix(in srgb,var(--fg),8%);background:color-mix(in srgb,var(--fg),2%)}
.cp-tab--l3:hover{color:var(--fg);border-color:var(--fg)}
.cp-tab--l3.active{color:var(--fg);border-color:var(--fg);background:color-mix(in srgb,var(--fg),6%)}

.cp-slide-enter-active{transition:all .2s ease-out;overflow:hidden}
.cp-slide-leave-active{transition:all .12s ease-in;overflow:hidden}
.cp-slide-enter-from,.cp-slide-leave-to{opacity:0;max-height:0}
.cp-slide-enter-to,.cp-slide-leave-from{max-height:200px}
</style>
