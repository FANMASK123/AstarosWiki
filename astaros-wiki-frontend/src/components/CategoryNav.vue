<template>
  <div style="height:100%;display:flex;flex-direction:column">
    <div style="padding:10px 14px;border-bottom:1px solid var(--border)">
      <span class="eyebrow">功能菜单</span>
    </div>
    <div style="flex:1;overflow-y:auto">
      <div v-if="categoryStore.loading" class="loading"><div class="spinner"></div></div>
      <div v-else-if="categories.length===0" style="padding:16px"><span class="text-muted">暂无</span></div>
      <ul v-else class="sm-root">
        <li class="sm-node">
          <div class="sm-row" @click="catOpen = !catOpen">
            <span class="sm-label">分类导航</span>
            <span :class="['sm-arrow', catOpen && 'open']">▸</span>
          </div>
          <Transition name="sm-slide">
            <ul v-if="catOpen" class="sm-dropdown">
              <li v-for="c in categories" :key="c.id">
                <div class="sm-row sm-sub" @click="toggle(c)">
                  <router-link :to="`/category/${c.id}`" class="sm-link" @click.stop>
                    <span class="sm-name">{{ c.name }}</span>
                    <span v-if="c.children?.length" class="sm-count">{{ c.children.length }}</span>
                  </router-link>
                  <button v-if="c.children?.length" :class="['sm-arrow', open1.has(c.id) && 'open']" @click.stop="toggle(c)">▸</button>
                </div>
                <Transition name="sm-slide">
                  <ul v-if="c.children?.length && open1.has(c.id)" class="sm-dropdown sm-l2">
                    <li v-for="ch in c.children" :key="ch.id">
                      <div class="sm-row sm-sub">
                        <router-link :to="`/category/${ch.id}`" class="sm-link" @click.stop>
                          <span class="sm-name">{{ ch.name }}</span>
                          <span v-if="ch.children?.length" class="sm-count">{{ ch.children.length }}</span>
                        </router-link>
                        <button v-if="ch.children?.length" :class="['sm-arrow', open2.has(ch.id) && 'open']" @click.stop="t2(ch)">▸</button>
                      </div>
                      <Transition name="sm-slide">
                        <ul v-if="ch.children?.length && open2.has(ch.id)" class="sm-dropdown sm-l3">
                          <li v-for="gc in ch.children" :key="gc.id">
                            <router-link :to="`/category/${gc.id}`" class="sm-row sm-sub sm-link">
                              <span class="sm-name">{{ gc.name }}</span>
                            </router-link>
                          </li>
                        </ul>
                      </Transition>
                    </li>
                  </ul>
                </Transition>
              </li>
            </ul>
          </Transition>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useCategoryStore } from '../stores/category'
import type { CategoryDTO } from '../api/category'

const categoryStore = useCategoryStore()
const catOpen = ref(false)
const open1 = ref(new Set<number>())
const open2 = ref(new Set<number>())
const categories = computed(() => categoryStore.categories)

onMounted(async () => { await categoryStore.fetchCategories() })

function toggle(c: CategoryDTO) {
  if (open1.value.has(c.id)) open1.value.delete(c.id); else open1.value.add(c.id)
  open1.value = new Set(open1.value)
}
function t2(c: CategoryDTO) {
  if (open2.value.has(c.id)) open2.value.delete(c.id); else open2.value.add(c.id)
  open2.value = new Set(open2.value)
}
</script>

<style scoped>
.sm-root { list-style: none; padding: 0; margin: 0; }
.sm-node { border-bottom: 1px solid color-mix(in srgb, var(--fg) 4%, transparent); }
.sm-row { display: flex; align-items: center; min-height: 32px; cursor: pointer; padding: 0 6px; transition: background .12s; }
.sm-row:hover { background: color-mix(in srgb, var(--fg) 4%, transparent); }
.sm-label { flex: 1; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: .04em; padding: 6px 8px; color: var(--fg); }
.sm-arrow { width: 20px; height: 20px; display: inline-flex; align-items: center; justify-content: center; background: none; border: none; color: var(--muted); cursor: pointer; flex-shrink: 0; font-size: 8px; transition: transform .2s, color .15s; }
.sm-arrow:hover { color: var(--fg); }
.sm-arrow.open { transform: rotate(90deg); }
.sm-sub { padding-left: 4px; }
.sm-link { flex: 1; display: flex; align-items: center; gap: 4px; font-size: 11px; color: var(--fg); text-decoration: none; padding: 5px 2px; min-width: 0; }
.sm-name { flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.sm-count { font-family: var(--mono); font-size: 8px; color: var(--muted); background: color-mix(in srgb, var(--fg) 5%, transparent); padding: 1px 3px; }

.sm-dropdown { list-style: none; padding: 0; margin: 0; border-top: 1px solid color-mix(in srgb, var(--fg) 3%, transparent); background: color-mix(in srgb, var(--fg) 1.5%, transparent); overflow: hidden; }
.sm-l2 { background: color-mix(in srgb, var(--fg) 2%, transparent); padding-left: 10px; }
.sm-l3 { background: color-mix(in srgb, var(--fg) 2.5%, transparent); padding-left: 10px; }

.sm-slide-enter-active { transition: all .18s ease-out; overflow: hidden; }
.sm-slide-leave-active { transition: all .12s ease-in; overflow: hidden; }
.sm-slide-enter-from, .sm-slide-leave-to { opacity: 0; max-height: 0; }
</style>
