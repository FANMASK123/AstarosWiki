<template>
  <div class="cat-root-wrapper">
    <!-- Loading -->
    <div v-if="categoryStore.loading" class="loading"><div class="spinner"></div></div>

    <!-- Empty -->
    <div v-else-if="categories.length === 0" style="padding: 24px 14px">
      <span class="text-muted">暂无分类</span>
    </div>

    <!-- Category list -->
    <div v-else class="cat-list">
      <div
        v-for="cat in categories"
        :key="cat.id"
        class="cat-row"
        :class="{ 'cat-row--active': activeL1 === cat.id }"
      >
        <router-link :to="`/category/${cat.id}`" class="cat-link" @click.stop>
          <span class="cat-name">{{ cat.name }}</span>
          <span v-if="cat.children?.length" class="cat-badge">{{ cat.children.length }}</span>
        </router-link>
        <button
          v-if="cat.children?.length"
          class="cat-arrow-btn"
          :class="{ open: activeL1 === cat.id }"
          @click.stop="toggleL1(cat.id)"
          title="展开子分类"
        >▸</button>
        <span v-else style="width:28px;flex-shrink:0"></span>
      </div>
    </div>

    <!-- Sub-panel L1 → L2 -->
    <Transition name="slide">
      <div v-if="activeL1 !== null && activeL1Children.length" class="cat-sub-panel">
        <div class="panel-hd">
          <span class="panel-title">{{ activeL1Label }}</span>
          <button class="panel-close" @click="activeL1 = null">✕</button>
        </div>
        <div class="panel-bd">
          <div
            v-for="child in activeL1Children"
            :key="child.id"
            class="cat-row"
            :class="{ 'cat-row--active': activeL2 === child.id }"
          >
            <router-link :to="`/category/${child.id}`" class="cat-link" @click.stop>
              <span class="cat-name">{{ child.name }}</span>
              <span v-if="child.children?.length" class="cat-badge">{{ child.children.length }}</span>
            </router-link>
            <button
              v-if="child.children?.length"
              class="cat-arrow-btn"
              :class="{ open: activeL2 === child.id }"
              @click.stop="toggleL2(child.id)"
              title="展开子分类"
            >▸</button>
            <span v-else style="width:28px;flex-shrink:0"></span>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Sub-panel L2 → L3 -->
    <Transition name="slide">
      <div v-if="activeL2 !== null && activeL2Children.length" class="cat-sub-panel cat-l3">
        <div class="panel-hd">
          <span class="panel-title">{{ activeL2Label }}</span>
          <button class="panel-close" @click="activeL2 = null">✕</button>
        </div>
        <div class="panel-bd">
          <div v-for="gc in activeL2Children" :key="gc.id" class="cat-row">
            <router-link :to="`/category/${gc.id}`" class="cat-link" @click.stop>
              <span class="cat-name">{{ gc.name }}</span>
            </router-link>
            <span style="width:28px;flex-shrink:0"></span>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useCategoryStore } from '../stores/category'

const categoryStore = useCategoryStore()
const categories = computed(() => categoryStore.categories)

// Mutually exclusive: only one level open at a time
const activeL1 = ref<number | null>(null)
const activeL2 = ref<number | null>(null)

const activeL1Label = computed(() =>
  categories.value.find((c) => c.id === activeL1.value)?.name ?? ''
)
const activeL1Children = computed(() =>
  categories.value.find((c) => c.id === activeL1.value)?.children ?? []
)
const activeL2Label = computed(() =>
  activeL1Children.value.find((c) => c.id === activeL2.value)?.name ?? ''
)
const activeL2Children = computed(() =>
  activeL1Children.value.find((c) => c.id === activeL2.value)?.children ?? []
)

onMounted(async () => {
  await categoryStore.fetchCategories()
})

function toggleL1(id: number) {
  activeL2.value = null // close L2 when switching L1
  activeL1.value = activeL1.value === id ? null : id
}

function toggleL2(id: number) {
  activeL2.value = activeL2.value === id ? null : id
}
</script>

<style scoped>
/* ── Root ────────────────────────── */
.cat-root-wrapper {
  position: relative;
}

/* ── List ────────────────────────── */
.cat-list {
  padding: 4px 0;
}

/* ── Row ─────────────────────────── */
.cat-row {
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--border);
  transition: background 0.12s;
}
.cat-row:hover {
  background: color-mix(in srgb, var(--fg) 4%, transparent);
}
.cat-row--active {
  background: color-mix(in srgb, var(--fg) 8%, transparent);
}
.cat-row:last-child {
  border-bottom: none;
}

.cat-link {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  text-decoration: none;
  color: var(--fg);
  font-size: 13px;
  min-width: 0;
  transition: background 0.12s;
}
.cat-link:hover {
  background: color-mix(in srgb, var(--fg) 3%, transparent);
}

.cat-name {
  flex: 1;
  /* NO truncation — show full name */
  overflow: visible;
  white-space: normal;
  word-break: break-word;
  line-height: 1.4;
}

.cat-badge {
  font-family: var(--mono);
  font-size: 10px;
  color: var(--muted);
  background: color-mix(in srgb, var(--fg) 8%, transparent);
  padding: 1px 5px;
  flex-shrink: 0;
}

/* ── Arrow button ────────────────── */
.cat-arrow-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  color: var(--muted);
  cursor: pointer;
  font-size: 10px;
  flex-shrink: 0;
  transition: transform 0.2s, color 0.15s;
  margin-right: 2px;
}
.cat-arrow-btn:hover {
  color: var(--fg);
}
.cat-arrow-btn.open {
  transform: rotate(90deg);
  color: var(--fg);
}

/* ── Sub-panel ───────────────────── */
.cat-sub-panel {
  position: fixed;
  top: 56px;
  bottom: 0;
  width: 220px;
  background: var(--card);
  border-right: 1px solid var(--border);
  z-index: 31;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
/* L2 panel: right of L1 panel */
.cat-sub-panel:not(.cat-l3) {
  left: 410px; /* sidebar(170) + panel(240) */
}
/* L3 panel: right of L2 panel */
.cat-l3 {
  left: 630px; /* 410 + 220 */
}

.panel-hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-bottom: 1px solid var(--border);
  background: var(--bg);
  flex-shrink: 0;
}

.panel-title {
  font-weight: 700;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.panel-close {
  background: none;
  border: none;
  color: var(--muted);
  cursor: pointer;
  font-size: 14px;
  padding: 2px 4px;
  transition: color 0.15s;
}
.panel-close:hover {
  color: var(--fg);
}

.panel-bd {
  flex: 1;
  overflow-y: auto;
}

/* ── Transitions ─────────────────── */
.slide-enter-active {
  transition: transform 0.2s ease-out, opacity 0.2s ease-out;
}
.slide-leave-active {
  transition: transform 0.12s ease-in, opacity 0.12s ease-in;
}
.slide-enter-from {
  transform: translateX(-12px);
  opacity: 0;
}
.slide-leave-to {
  transform: translateX(-12px);
  opacity: 0;
}

/* ── Loading ─────────────────────── */
.loading {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}
.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--border);
  border-top-color: var(--fg);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.text-muted {
  font-size: 13px;
  color: var(--muted);
}
</style>
