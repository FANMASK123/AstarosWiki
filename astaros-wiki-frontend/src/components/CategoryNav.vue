<template>
  <div class="cat-nav">
    <div v-if="categoryStore.loading" class="loading"><div class="spinner"></div></div>
    <div v-else-if="categories.length===0" style="padding:16px"><span class="text-muted">暂无分类</span></div>

    <!-- Level 1 -->
    <template v-else>
      <h3 class="cat-h">分类导航</h3>
      <div class="cat-row-l1">
        <button
          v-for="c in categories"
          :key="c.id"
          class="cat-btn-l1"
          :class="{ active: l1?.id === c.id }"
          @click="pickL1(c)"
        >{{ c.name }}</button>
      </div>

      <!-- Level 2 — slides in below -->
      <Transition name="cat-in">
        <div v-if="l1?.children?.length" class="cat-l2-section">
          <h3 class="cat-h cat-h-sub">{{ l1.name }}</h3>
          <div class="cat-row-l2">
            <button class="cat-btn-l2 cat-btn-all" :class="{ active: !l2 }" @click="navAll">全部</button>
            <button
              v-for="ch in l1.children"
              :key="ch.id"
              class="cat-btn-l2"
              :class="{ active: l2?.id === ch.id }"
              @click="pickL2(ch)"
            >{{ ch.name }}</button>
          </div>

          <!-- Level 3 — slides in below -->
          <Transition name="cat-in">
            <div v-if="l2?.children?.length" class="cat-l3-section">
              <h3 class="cat-h cat-h-sub">{{ l2.name }}</h3>
              <div class="cat-row-l3">
                <button class="cat-btn-l3 cat-btn-all" @click="nav(l2!)">全部</button>
                <button
                  v-for="gc in l2.children"
                  :key="gc.id"
                  class="cat-btn-l3"
                  @click="nav(gc)"
                >{{ gc.name }}</button>
              </div>
            </div>
          </Transition>
        </div>
      </Transition>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCategoryStore } from '../stores/category'
import type { CategoryDTO } from '../api/category'

const router = useRouter()
const categoryStore = useCategoryStore()
const l1 = ref<CategoryDTO | null>(null)
const l2 = ref<CategoryDTO | null>(null)
const categories = computed(() => categoryStore.categories)

onMounted(async () => { await categoryStore.fetchCategories() })

function pickL1(c: CategoryDTO) {
  if (l1.value?.id === c.id) { l1.value = null; l2.value = null }
  else { l1.value = c; l2.value = null }
}
function pickL2(c: CategoryDTO) {
  if (l2.value?.id === c.id) l2.value = null
  else l2.value = c
}
function navAll() { if (l1.value) router.push(`/category/${l1.value.id}`); reset() }
function nav(c: CategoryDTO) { router.push(`/category/${c.id}`); reset() }
function reset() { l1.value = null; l2.value = null }
</script>

<style scoped>
.cat-nav { padding: 0; }

/* ── Headings ─────────────────────────────────── */
.cat-h {
  font-family: var(--font); font-size: 12px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .08em;
  color: var(--fg); margin: 0 0 6px; padding: 0;
}
.cat-h-sub { margin-top: 10px; font-size: 11px; color: var(--muted); }

/* ── Level 1 row ──────────────────────────────── */
.cat-row-l1 { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 2px; }
.cat-btn-l1 {
  flex: 1 1 auto; min-width: 0; padding: 7px 8px;
  font-family: var(--font); font-size: 12px; font-weight: 600;
  text-transform: uppercase; letter-spacing: .04em;
  background: none; border: 1px solid var(--border); color: var(--muted);
  cursor: pointer; transition: all .15s ease-out;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.cat-btn-l1:hover { color: var(--fg); border-color: var(--muted); background: color-mix(in srgb, var(--fg) 5%, transparent); transform: translateY(-1px); }
.cat-btn-l1.active { color: var(--fg); border-color: var(--fg); background: color-mix(in srgb, var(--fg) 10%, transparent); }

/* ── Level 2 row ──────────────────────────────── */
.cat-l2-section { margin: 4px 0 2px; }
.cat-row-l2 { display: flex; flex-wrap: wrap; gap: 4px; }
.cat-btn-l2 {
  padding: 5px 10px; font-family: var(--mono); font-size: 10px;
  text-transform: uppercase; letter-spacing: .04em;
  background: none; border: 1px solid var(--border); color: var(--muted);
  cursor: pointer; transition: all .15s ease-out;
}
.cat-btn-l2:hover { color: var(--fg); border-color: var(--muted); background: color-mix(in srgb, var(--fg) 5%, transparent); transform: translateY(-1px); }
.cat-btn-l2.active { color: var(--fg); border-color: var(--fg); background: color-mix(in srgb, var(--fg) 8%, transparent); }

/* ── Level 3 row ──────────────────────────────── */
.cat-l3-section { margin: 4px 0 2px; }
.cat-row-l3 { display: flex; flex-wrap: wrap; gap: 4px; }
.cat-btn-l3 {
  padding: 4px 8px; font-family: var(--mono); font-size: 10px;
  text-transform: uppercase; letter-spacing: .04em;
  background: color-mix(in srgb, var(--fg) 3%, transparent);
  border: 1px solid color-mix(in srgb, var(--fg) 6%, transparent);
  color: var(--muted); cursor: pointer; transition: all .15s ease-out;
}
.cat-btn-l3:hover { color: var(--fg); border-color: var(--fg); background: color-mix(in srgb, var(--fg) 6%, transparent); transform: translateY(-1px); }

/* ── "全部" button ────────────────────────────── */
.cat-btn-all { font-style: italic; opacity: .65; }

/* ── In animation ─────────────────────────────── */
.cat-in-enter-active { transition: all .2s ease-out; }
.cat-in-leave-active { transition: all .12s ease-in; }
.cat-in-enter-from { opacity: 0; max-height: 0; }
.cat-in-leave-to { opacity: 0; max-height: 0; }
.cat-in-enter-to, .cat-in-leave-from { max-height: 120px; }
</style>
