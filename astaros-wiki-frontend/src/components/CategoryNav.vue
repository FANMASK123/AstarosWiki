<template>
  <div class="sidebar-cat">
    <div v-if="categoryStore.loading" class="loading"><div class="spinner"></div></div>
    <div v-else-if="categories.length===0" style="padding:16px"><span class="text-muted">暂无分类</span></div>
    <ul v-else class="sc-root">
      <li v-for="c in categories" :key="c.id" class="sc-node">
        <div class="sc-row" :class="{ active: l1?.id === c.id }">
          <router-link :to="`/category/${c.id}`" class="sc-link" @click="emit('navigate')">
            <span class="sc-label">{{ c.name }}</span>
            <span v-if="c.children?.length" class="sc-count">{{ c.children.length }}</span>
          </router-link>
          <button v-if="c.children?.length" class="sc-arrow" :class="{ open: l1?.id === c.id }" @click.stop="toggleL1(c)" title="展开">▸</button>
        </div>

        <!-- L2 slide panel -->
        <Transition name="sc-slide">
          <div v-if="l1?.id === c.id && c.children?.length" class="sc-l2-panel">
            <div class="sc-l2-header">
              <span class="sc-l2-title">{{ c.name }}</span>
              <button class="sc-close" @click="l1=null;l2=null">✕</button>
            </div>
            <ul class="sc-l2-list">
              <li v-for="ch in c.children" :key="ch.id">
                <div class="sc-row sc-row--l2" :class="{ active: l2?.id === ch.id }">
                  <router-link :to="`/category/${ch.id}`" class="sc-link" @click="emit('navigate')">
                    <span class="sc-label">{{ ch.name }}</span>
                    <span v-if="ch.children?.length" class="sc-count">{{ ch.children.length }}</span>
                  </router-link>
                  <button v-if="ch.children?.length" class="sc-arrow" :class="{ open: l2?.id === ch.id }" @click.stop="toggleL2(ch)" title="展开">▸</button>
                </div>

                <!-- L3 slide panel -->
                <Transition name="sc-slide">
                  <div v-if="l2?.id === ch.id && ch.children?.length" class="sc-l3-panel">
                    <div class="sc-l2-header">
                      <span class="sc-l2-title">{{ ch.name }}</span>
                      <button class="sc-close" @click="l2=null">✕</button>
                    </div>
                    <ul class="sc-l2-list">
                      <li v-for="gc in ch.children" :key="gc.id">
                        <router-link :to="`/category/${gc.id}`" class="sc-row sc-row--l3" @click="emit('navigate')">
                          <span class="sc-link"><span class="sc-label">{{ gc.name }}</span></span>
                        </router-link>
                      </li>
                    </ul>
                  </div>
                </Transition>
              </li>
            </ul>
          </div>
        </Transition>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useCategoryStore } from '../stores/category'
import type { CategoryDTO } from '../api/category'

const emit = defineEmits<{ navigate: [] }>()
const categoryStore = useCategoryStore()
const l1 = ref<CategoryDTO | null>(null)
const l2 = ref<CategoryDTO | null>(null)
const categories = computed(() => categoryStore.categories)

onMounted(async () => { await categoryStore.fetchCategories() })

function toggleL1(c: CategoryDTO) {
  if (l1.value?.id === c.id) { l1.value = null; l2.value = null }
  else { l1.value = c; l2.value = null }
}
function toggleL2(c: CategoryDTO) {
  l2.value = l2.value?.id === c.id ? null : c
}
</script>

<style scoped>
.sidebar-cat { height: 100%; display: flex; flex-direction: column; }
.sc-root { list-style: none; padding: 0; margin: 0; flex: 1; overflow-y: auto; }
.sc-node { border-bottom: 1px solid color-mix(in srgb, var(--fg) 4%, transparent); }
.sc-node:last-child { border-bottom: none; }
.sc-row { display: flex; align-items: center; min-height: 34px; cursor: pointer; transition: background .12s; }
.sc-row:hover { background: color-mix(in srgb, var(--fg) 4%, transparent); }
.sc-row.active { background: color-mix(in srgb, var(--fg) 6%, transparent); }
.sc-link { flex: 1; display: flex; align-items: center; gap: 4px; padding: 6px 10px; font-size: 12px; color: var(--fg); text-decoration: none; min-width: 0; }
.sc-label { flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.sc-count { font-family: var(--mono); font-size: 9px; color: var(--muted); background: color-mix(in srgb, var(--fg) 6%, transparent); padding: 1px 4px; flex-shrink: 0; }
.sc-arrow { width: 22px; height: 22px; display: flex; align-items: center; justify-content: center; background: none; border: none; color: var(--muted); cursor: pointer; flex-shrink: 0; font-size: 8px; transition: transform .2s, color .15s; margin-right: 2px; }
.sc-arrow:hover { color: var(--fg); }
.sc-arrow.open { transform: rotate(90deg); }

/* L2 panel — slides in from right within sidebar */
.sc-l2-panel { position: absolute; left: 200px; top: 52px; bottom: 0; width: 220px; background: var(--card); border-right: 1px solid var(--border); z-index: 35; display: flex; flex-direction: column; overflow: hidden; }
.sc-l2-header { display: flex; align-items: center; justify-content: space-between; padding: 8px 12px; border-bottom: 1px solid var(--border); background: var(--bg); flex-shrink: 0; }
.sc-l2-title { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .06em; color: var(--fg); }
.sc-close { background: none; border: none; font-size: 12px; color: var(--muted); cursor: pointer; padding: 2px 4px; transition: color .15s; }
.sc-close:hover { color: var(--fg); }
.sc-l2-list { list-style: none; padding: 0; margin: 0; flex: 1; overflow-y: auto; }
.sc-row--l2 { padding-left: 4px; }
.sc-row--l3 { padding-left: 12px; }

/* L3 panel — slides from right of L2 */
.sc-l3-panel { position: absolute; left: 220px; top: 0; bottom: 0; width: 200px; background: var(--card); border-right: 1px solid var(--border); z-index: 36; display: flex; flex-direction: column; overflow: hidden; }

/* Slide transitions */
.sc-slide-enter-active { transition: all .2s ease-out; }
.sc-slide-leave-active { transition: all .12s ease-in; }
.sc-slide-enter-from { opacity: 0; transform: translateX(-12px); }
.sc-slide-leave-to { opacity: 0; transform: translateX(-12px); }
</style>
