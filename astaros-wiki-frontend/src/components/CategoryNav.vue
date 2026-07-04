<template>
  <div class="cat-panel">
    <div v-if="categoryStore.loading" class="loading"><div class="spinner"></div></div>
    <div v-else-if="categories.length===0" style="padding:16px"><span class="text-muted">暂无分类</span></div>
    <template v-else>
      <!-- Level 1: horizontal tabs -->
      <div class="cat-l1-tabs">
        <button
          v-for="c in categories"
          :key="c.id"
          class="cat-l1-tab"
          :class="{ active: l1?.id === c.id }"
          @click="pickL1(c)"
        >{{ c.name }}</button>
      </div>

      <!-- Level 2: slide panel -->
      <Transition name="cat-slide">
        <div v-if="l1?.children?.length" class="cat-l2-panel">
          <div class="cat-l2-tabs">
            <button
              v-for="ch in l1.children"
              :key="ch.id"
              class="cat-l2-tab"
              :class="{ active: l2?.id === ch.id }"
              @click="pickL2(ch)"
            >{{ ch.name }}</button>
            <button class="cat-l2-tab cat-l2-all" :class="{ active: !l2 }" @click="navAll">全部</button>
          </div>

          <!-- Level 3: slide panel -->
          <Transition name="cat-slide">
            <div v-if="l2?.children?.length" class="cat-l3-panel">
              <div class="cat-l3-tabs">
                <button
                  v-for="gc in l2.children"
                  :key="gc.id"
                  class="cat-l3-tab"
                  @click="nav(gc)"
                >{{ gc.name }}</button>
                <button class="cat-l3-tab cat-l3-all" @click="nav(l2)">全部</button>
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
  l1.value = l1.value?.id === c.id ? null : c
  l2.value = null
}
function pickL2(c: CategoryDTO) {
  l2.value = l2.value?.id === c.id ? null : c
}
function navAll() { if (l1.value) router.push(`/category/${l1.value.id}`); l1.value = null; l2.value = null }
function nav(c: CategoryDTO) { router.push(`/category/${c.id}`); l1.value = null; l2.value = null }
</script>

<style scoped>
.cat-panel { padding: 0; }

/* ── Level 1 tabs ──────────────────────────────── */
.cat-l1-tabs { display: flex; flex-wrap: wrap; gap: 0; border-bottom: 1px solid var(--border); }
.cat-l1-tab {
  flex: 1; min-width: 0; padding: 8px 6px; font-size: 11px; font-family: var(--font);
  text-transform: uppercase; letter-spacing: .06em; font-weight: 600;
  background: none; border: none; border-bottom: 2px solid transparent;
  color: var(--muted); cursor: pointer; transition: color .15s, border-color .15s, background .15s;
  white-space: nowrap; text-overflow: ellipsis; overflow: hidden;
}
.cat-l1-tab:hover { color: var(--fg); background: color-mix(in srgb, var(--fg) 4%, transparent); }
.cat-l1-tab.active { color: var(--fg); border-bottom-color: var(--fg); }

/* ── Level 2 panel ─────────────────────────────── */
.cat-l2-panel { margin-top: 4px; padding: 6px 0; border-bottom: 1px solid color-mix(in srgb, var(--fg) 5%, transparent); }
.cat-l2-tabs { display: flex; flex-wrap: wrap; gap: 3px; }
.cat-l2-tab {
  padding: 4px 10px; font-size: 10px; font-family: var(--mono);
  text-transform: uppercase; letter-spacing: .06em;
  background: none; border: 1px solid var(--border); color: var(--muted);
  cursor: pointer; transition: all .15s;
}
.cat-l2-tab:hover { color: var(--fg); border-color: var(--muted); background: color-mix(in srgb, var(--fg) 5%, transparent); transform: translateY(-1px); }
.cat-l2-tab.active { color: var(--fg); border-color: var(--fg); background: color-mix(in srgb, var(--fg) 8%, transparent); }
.cat-l2-all { font-style: italic; opacity: .7; }

/* ── Level 3 panel ─────────────────────────────── */
.cat-l3-panel { margin-top: 4px; padding: 4px 0; }
.cat-l3-tabs { display: flex; flex-wrap: wrap; gap: 3px; }
.cat-l3-tab {
  padding: 3px 8px; font-size: 10px; font-family: var(--mono);
  text-transform: uppercase; letter-spacing: .04em;
  background: color-mix(in srgb, var(--fg) 3%, transparent); border: 1px solid color-mix(in srgb, var(--fg) 6%, transparent);
  color: var(--muted); cursor: pointer; transition: all .15s;
}
.cat-l3-tab:hover { color: var(--fg); border-color: var(--fg); background: color-mix(in srgb, var(--fg) 6%, transparent); transform: translateY(-1px); }
.cat-l3-all { font-style: italic; opacity: .7; }

/* ── Slide transition ──────────────────────────── */
.cat-slide-enter-active { transition: all .2s ease-out; }
.cat-slide-leave-active { transition: all .12s ease-in; }
.cat-slide-enter-from { opacity: 0; max-height: 0; transform: translateY(-6px); }
.cat-slide-leave-to { opacity: 0; max-height: 0; transform: translateY(-4px); }
.cat-slide-enter-to, .cat-slide-leave-from { max-height: 200px; }
</style>
