<template>
  <div style="height:100%;display:flex;flex-direction:column">
    <!-- 功能菜单 header -->
    <div style="padding:10px 14px;border-bottom:1px solid var(--border)">
      <span class="eyebrow">功能菜单</span>
    </div>

    <!-- 分类导航 toggle -->
    <div class="fn-row" :class="{ active: open }" @click="open = !open">
      <span class="fn-label">分类导航</span>
      <span :class="['fn-arrow', open && 'open']">▸</span>
    </div>

    <div style="flex:1"></div>

    <!-- L1 slide panel -->
    <Transition name="sc-slide">
      <div v-if="open" class="sc-panel sc-panel--l1">
        <div v-if="categoryStore.loading" style="padding:16px"><div class="spinner" style="width:16px;height:16px"></div></div>
        <div v-else-if="categories.length===0" style="padding:16px"><span class="text-muted" style="font-size:11px">暂无分类</span></div>
        <ul v-else class="sc-list">
          <li v-for="c in categories" :key="c.id">
            <div class="sc-row" :class="{ active: l1?.id === c.id }">
              <router-link :to="`/category/${c.id}`" class="sc-link" @click="open=false">
                <span class="sc-name">{{ c.name }}</span>
                <span v-if="c.children?.length" class="sc-count">{{ c.children.length }}</span>
              </router-link>
              <button v-if="c.children?.length" :class="['sc-arr', l1?.id === c.id && 'open']" @click.stop="toggleL1(c)">▸</button>
            </div>
          </li>
        </ul>
      </div>
    </Transition>

    <!-- L2 slide panel -->
    <Transition name="sc-slide">
      <div v-if="open && l1?.children?.length" class="sc-panel sc-panel--l2">
        <div class="sc-head">
          <span class="sc-head-title">{{ l1.name }}</span>
          <button class="sc-head-close" @click="l1=null;l2=null">✕</button>
        </div>
        <ul class="sc-list">
          <li v-for="ch in l1.children" :key="ch.id">
            <div class="sc-row" :class="{ active: l2?.id === ch.id }">
              <router-link :to="`/category/${ch.id}`" class="sc-link" @click="closeAll">
                <span class="sc-name">{{ ch.name }}</span>
                <span v-if="ch.children?.length" class="sc-count">{{ ch.children.length }}</span>
              </router-link>
              <button v-if="ch.children?.length" :class="['sc-arr', l2?.id === ch.id && 'open']" @click.stop="toggleL2(ch)">▸</button>
            </div>
          </li>
        </ul>
      </div>
    </Transition>

    <!-- L3 slide panel -->
    <Transition name="sc-slide">
      <div v-if="open && l2?.children?.length" class="sc-panel sc-panel--l3">
        <div class="sc-head">
          <span class="sc-head-title">{{ l2.name }}</span>
          <button class="sc-head-close" @click="l2=null">✕</button>
        </div>
        <ul class="sc-list">
          <li v-for="gc in l2.children" :key="gc.id">
            <router-link :to="`/category/${gc.id}`" class="sc-row sc-link" @click="closeAll">
              <span class="sc-name">{{ gc.name }}</span>
            </router-link>
          </li>
        </ul>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useCategoryStore } from '../stores/category'
import type { CategoryDTO } from '../api/category'

const categoryStore = useCategoryStore()
const open = ref(false)
const l1 = ref<CategoryDTO | null>(null)
const l2 = ref<CategoryDTO | null>(null)
const categories = computed(() => categoryStore.categories)

onMounted(async () => { await categoryStore.fetchCategories() })

function toggleL1(c: CategoryDTO) {
  l1.value = l1.value?.id === c.id ? null : c; l2.value = null
}
function toggleL2(c: CategoryDTO) {
  l2.value = l2.value?.id === c.id ? null : c
}
function closeAll() { open.value = false; l1.value = null; l2.value = null }
</script>

<style scoped>
.fn-row { display: flex; align-items: center; min-height: 36px; cursor: pointer; padding: 0 10px; transition: background .12s; }
.fn-row:hover, .fn-row.active { background: color-mix(in srgb, var(--fg) 5%, transparent); }
.fn-label { flex: 1; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: .04em; color: var(--fg); }
.fn-arrow { font-size: 8px; color: var(--muted); transition: transform .2s; flex-shrink: 0; margin-right: 4px; }
.fn-arrow.open { transform: rotate(90deg); }

/* Slide panels */
.sc-panel { position: fixed; top: 52px; bottom: 0; background: var(--card); border-right: 1px solid var(--border); z-index: 45; display: flex; flex-direction: column; overflow: hidden; }
.sc-panel--l1 { left: 200px; width: 200px; }
.sc-panel--l2 { left: 400px; width: 200px; }
.sc-panel--l3 { left: 600px; width: 180px; }
.sc-head { display: flex; align-items: center; justify-content: space-between; padding: 8px 10px; border-bottom: 1px solid var(--border); flex-shrink: 0; background: var(--bg); }
.sc-head-title { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .05em; color: var(--fg); }
.sc-head-close { background: none; border: none; font-size: 11px; color: var(--muted); cursor: pointer; padding: 2px 4px; transition: color .12s; }
.sc-head-close:hover { color: var(--fg); }
.sc-list { list-style: none; padding: 0; margin: 0; flex: 1; overflow-y: auto; }
.sc-row { display: flex; align-items: center; min-height: 32px; cursor: pointer; transition: background .12s; border-bottom: 1px solid var(--cat-children-border); }
.sc-row:hover { background: var(--cat-hover); }
.sc-row.active { background: var(--hover); }
.sc-link { flex: 1; display: flex; align-items: center; gap: 4px; padding: 6px 10px; font-size: 11px; color: var(--fg); text-decoration: none; min-width: 0; }
.sc-name { flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.sc-count { font-family: var(--mono); font-size: 8px; color: var(--muted); background: var(--cat-count); padding: 1px 3px; flex-shrink: 0; }
.sc-arr { width: 20px; height: 20px; display: flex; align-items: center; justify-content: center; background: none; border: none; color: var(--muted); cursor: pointer; flex-shrink: 0; font-size: 7px; transition: transform .2s, color .12s; margin-right: 2px; }
.sc-arr:hover { color: var(--fg); }
.sc-arr.open { transform: rotate(90deg); }

/* Slide transitions */
.sc-slide-enter-active { transition: opacity .18s ease-out, transform .18s ease-out; }
.sc-slide-leave-active { transition: opacity .1s ease-in, transform .1s ease-in; }
.sc-slide-enter-from { opacity: 0; transform: translateX(-10px); }
.sc-slide-leave-to { opacity: 0; transform: translateX(-10px); }
</style>
