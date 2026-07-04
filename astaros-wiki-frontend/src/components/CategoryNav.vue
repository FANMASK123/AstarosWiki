<template>
  <div class="cat-tree">
    <!-- Loading -->
    <div v-if="categoryStore.loading" class="cat-loading">
      <div class="loading-pulse"></div>
    </div>

    <!-- Empty -->
    <div v-else-if="categories.length === 0" class="cat-empty">
      <p class="text-muted">暂无分类</p>
    </div>

    <!-- Tree -->
    <ul v-else class="tree-root">
      <li v-for="category in categories" :key="category.id" class="tree-node">
        <!-- Level 1 row -->
        <div class="tree-row" :class="{ expanded: isExpanded(category.id, 1) }">
          <!-- Expand toggle (if has children) -->
          <button
            v-if="category.children?.length"
            class="tree-toggle"
            :class="{ open: isExpanded(category.id, 1) }"
            @click.stop="toggle(category.id, 1)"
            :aria-label="isExpanded(category.id, 1) ? '折叠' : '展开'"
          >
            <span class="toggle-icon">▸</span>
          </button>
          <!-- Spacer when no children -->
          <span v-else class="tree-toggle tree-toggle--spacer"></span>

          <!-- Link -->
          <router-link :to="`/category/${category.id}`" class="tree-link">
            <el-icon class="tree-leaf-icon">
              <FolderOpened v-if="category.children?.length && isExpanded(category.id, 1)" />
              <Folder v-else />
            </el-icon>
            <span class="tree-label">{{ category.name }}</span>
            <span v-if="category.children?.length" class="tree-badge">{{
              category.children.length
            }}</span>
          </router-link>
        </div>

        <!-- Level 2 children (inline expand) -->
        <Transition name="tree-expand">
          <ul v-if="category.children?.length && isExpanded(category.id, 1)" class="tree-children">
            <li v-for="child in category.children" :key="child.id" class="tree-node">
              <!-- Level 2 row -->
              <div class="tree-row tree-row--child" :class="{ expanded: isExpanded(child.id, 2) }">
                <button
                  v-if="child.children?.length"
                  class="tree-toggle"
                  :class="{ open: isExpanded(child.id, 2) }"
                  @click.stop="toggle(child.id, 2)"
                  :aria-label="isExpanded(child.id, 2) ? '折叠' : '展开'"
                >
                  <span class="toggle-icon">▸</span>
                </button>
                <span v-else class="tree-toggle tree-toggle--spacer"></span>

                <router-link :to="`/category/${child.id}`" class="tree-link">
                  <el-icon class="tree-leaf-icon">
                    <FolderOpened v-if="child.children?.length && isExpanded(child.id, 2)" />
                    <Folder v-else />
                  </el-icon>
                  <span class="tree-label">{{ child.name }}</span>
                  <span v-if="child.children?.length" class="tree-badge">{{
                    child.children.length
                  }}</span>
                </router-link>
              </div>

              <!-- Level 3 children (inline expand) -->
              <Transition name="tree-expand">
                <ul v-if="child.children?.length && isExpanded(child.id, 2)" class="tree-children">
                  <li v-for="grandchild in child.children" :key="grandchild.id" class="tree-node">
                    <div class="tree-row tree-row--grandchild">
                      <span class="tree-toggle tree-toggle--spacer"></span>
                      <router-link :to="`/category/${grandchild.id}`" class="tree-link">
                        <el-icon class="tree-leaf-icon"><Folder /></el-icon>
                        <span class="tree-label">{{ grandchild.name }}</span>
                      </router-link>
                    </div>
                  </li>
                </ul>
              </Transition>
            </li>
          </ul>
        </Transition>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useCategoryStore } from '../stores/category'
import { FolderOpened, Folder } from '@element-plus/icons-vue'

const categoryStore = useCategoryStore()
const expandedLevel1 = ref<Set<number>>(new Set())
const expandedLevel2 = ref<Set<number>>(new Set())

const categories = computed(() => categoryStore.categories)

onMounted(async () => {
  await categoryStore.fetchCategories()
})

function toggle(id: number, level: 1 | 2) {
  const set = level === 1 ? expandedLevel1 : expandedLevel2
  if (set.value.has(id)) {
    set.value.delete(id)
  } else {
    set.value.add(id)
  }
  // Reactivity: reassign to trigger
  set.value = new Set(set.value)
}

function isExpanded(id: number, level: 1 | 2): boolean {
  return (level === 1 ? expandedLevel1 : expandedLevel2).value.has(id)
}
</script>

<style scoped>
/* ── Root ── */
.cat-tree {
  position: relative;
}
.tree-root {
  list-style: none;
  padding: 0;
  margin: 0;
}
.tree-root > .tree-node {
  border-bottom: 1px solid var(--color-border);
}
.tree-root > .tree-node:last-child {
  border-bottom: none;
}

/* ── Node row ── */
.tree-row {
  display: flex;
  align-items: center;
  min-height: 40px;
  transition: background 0.12s;
}
.tree-row:hover {
  background: color-mix(in srgb, var(--color-foreground) 5%, transparent);
}
.tree-row.expanded {
  background: color-mix(in srgb, var(--color-foreground) 6%, transparent);
}

/* Child indent */
.tree-row--child {
  padding-left: 0;
}
.tree-row--grandchild {
  padding-left: 0;
}

/* ── Toggle button ── */
.tree-toggle {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  color: var(--color-muted-foreground);
  cursor: pointer;
  flex-shrink: 0;
  transition: color 0.15s;
  margin-left: 4px;
}
.tree-toggle:hover {
  color: var(--color-foreground);
}
.tree-toggle--spacer {
  visibility: hidden;
  pointer-events: none;
}

/* Toggle icon rotation */
.toggle-icon {
  display: inline-block;
  font-size: 10px;
  line-height: 1;
  transition: transform 0.2s ease;
}
.tree-toggle.open .toggle-icon {
  transform: rotate(90deg);
}

/* ── Link ── */
.tree-link {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 8px 9px 0;
  text-decoration: none;
  color: var(--color-foreground);
  font-size: 13px;
  min-width: 0;
  transition: background 0.12s;
}
.tree-link.router-link-active {
  background: color-mix(in srgb, var(--color-foreground) 8%, transparent);
}

.tree-leaf-icon {
  font-size: 13px;
  color: var(--color-muted-foreground);
  flex-shrink: 0;
}

.tree-label {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-family: var(--font-display);
}

/* Badge */
.tree-badge {
  font-family: var(--font-mono);
  font-size: 10px;
  color: var(--color-muted-foreground);
  background: color-mix(in srgb, var(--color-foreground) 8%, transparent);
  padding: 1px 6px;
  flex-shrink: 0;
  margin-right: 6px;
}

/* ── Children list ── */
.tree-children {
  list-style: none;
  padding: 0;
  margin: 0;
  border-top: 1px solid color-mix(in srgb, var(--color-foreground) 4%, transparent);
  background: color-mix(in srgb, var(--color-foreground) 2%, transparent);
  overflow: hidden;
}

/* Level 2 children indent */
.tree-children .tree-row--child .tree-toggle {
  margin-left: 20px;
}
/* Level 3 children indent */
.tree-children .tree-row--grandchild .tree-toggle {
  margin-left: 36px;
}

/* ── Transition ── */
.tree-expand-enter-active {
  transition: all 0.2s ease-out;
  overflow: hidden;
}
.tree-expand-leave-active {
  transition: all 0.15s ease-in;
  overflow: hidden;
}
.tree-expand-enter-from,
.tree-expand-leave-to {
  opacity: 0;
  max-height: 0;
}
.tree-expand-enter-to,
.tree-expand-leave-from {
  opacity: 1;
  max-height: 600px;
}

/* ── Misc ── */
.cat-loading {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}
.cat-empty {
  padding: 24px 16px;
}
.text-muted {
  font-size: 13px;
  color: var(--color-muted-foreground);
}

/* Loading spinner */
.loading-pulse {
  width: 16px;
  height: 16px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-foreground);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
