<template>
  <Transition name="slide-up">
    <div v-if="visible" class="mobile-menu-overlay" @click="$emit('close')">
      <div class="mobile-menu-container" @click.stop>
        <div class="mobile-menu-header">
          <span class="mobile-menu-title">功能菜单</span>
          <el-icon class="mobile-menu-close" @click="$emit('close')">
            <Close />
          </el-icon>
        </div>

        <div class="mobile-menu-content">
          <div
            v-for="item in menuItems"
            :key="item.key"
            class="mobile-menu-item"
            :class="{ 'mobile-menu-item-active': activeMenuKey === item.key }"
            @click="toggleMenu(item.key)"
          >
            <el-icon class="mobile-menu-icon">
              <component :is="item.icon" />
            </el-icon>
            <span class="mobile-menu-label">{{ item.label }}</span>
            <el-icon class="mobile-menu-arrow" :class="{ rotate: activeMenuKey === item.key }">
              <ArrowRight />
            </el-icon>
          </div>

          <Transition name="slide-down">
            <div v-if="activeMenuKey === 'category'" class="mobile-submenu">
              <div class="mobile-category-section">
                <div v-if="categoryStore.loading" class="mobile-loading">加载中...</div>
                <div v-else>
                  <template v-for="category in categoryStore.categories" :key="category.id">
                    <div
                      class="mobile-category-item"
                      :class="{
                        'mobile-category-expanded': expandedCategories.includes(category.id),
                      }"
                      @click.self="toggleCategoryExpand(category.id)"
                    >
                      <div class="mobile-category-header">
                        <el-icon class="mobile-category-icon">
                          <FolderOpened />
                        </el-icon>
                        <span class="mobile-category-name" @click="handleCategoryClick(category)">
                          {{ category.name }}
                        </span>
                        <div
                          v-if="category.children && category.children.length > 0"
                          class="mobile-category-expand-btn"
                          @click="toggleCategoryExpand(category.id)"
                        >
                          <el-icon
                            class="mobile-category-arrow"
                            :class="{ rotate: expandedCategories.includes(category.id) }"
                          >
                            <ArrowDown />
                          </el-icon>
                        </div>
                      </div>
                      <div class="mobile-category-description">
                        {{ category.description || '暂无描述' }}
                      </div>

                      <Transition name="slide-down">
                        <div
                          v-if="
                            category.children &&
                            category.children.length > 0 &&
                            expandedCategories.includes(category.id)
                          "
                          class="mobile-category-children"
                        >
                          <template v-for="child in category.children" :key="child.id">
                            <div
                              class="mobile-category-item mobile-category-child"
                              :class="{
                                'mobile-category-expanded': expandedChildCategories.includes(
                                  child.id,
                                ),
                              }"
                              @click.self="toggleChildCategoryExpand(child.id)"
                            >
                              <div class="mobile-category-header">
                                <el-icon class="mobile-category-icon">
                                  <Folder />
                                </el-icon>
                                <span
                                  class="mobile-category-name"
                                  @click="handleCategoryClick(child)"
                                >
                                  {{ child.name }}
                                </span>
                                <div
                                  v-if="child.children && child.children.length > 0"
                                  class="mobile-category-expand-btn"
                                  @click="toggleChildCategoryExpand(child.id)"
                                >
                                  <el-icon
                                    class="mobile-category-arrow"
                                    :class="{ rotate: expandedChildCategories.includes(child.id) }"
                                  >
                                    <ArrowDown />
                                  </el-icon>
                                </div>
                              </div>
                              <div class="mobile-category-description">
                                {{ child.description || '暂无描述' }}
                              </div>

                              <Transition name="slide-down">
                                <div
                                  v-if="
                                    child.children &&
                                    child.children.length > 0 &&
                                    expandedChildCategories.includes(child.id)
                                  "
                                  class="mobile-category-children"
                                >
                                  <div
                                    v-for="grandchild in child.children"
                                    :key="grandchild.id"
                                    class="mobile-category-item mobile-category-grandchild"
                                    @click="handleCategoryClick(grandchild)"
                                  >
                                    <el-icon class="mobile-category-icon">
                                      <Document />
                                    </el-icon>
                                    <span class="mobile-category-name">{{ grandchild.name }}</span>
                                    <div class="mobile-category-description">
                                      {{ grandchild.description || '暂无描述' }}
                                    </div>
                                  </div>
                                </div>
                              </Transition>
                            </div>
                          </template>
                        </div>
                      </Transition>
                    </div>
                  </template>
                </div>
              </div>
            </div>
          </Transition>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCategoryStore } from '../stores/category'
import {
  ArrowRight,
  ArrowDown,
  FolderOpened,
  Folder,
  Document,
  Close,
} from '@element-plus/icons-vue'
import type { CategoryDTO } from '../api/category'

const router = useRouter()
const categoryStore = useCategoryStore()

const emit = defineEmits<{
  close: []
}>()

defineProps<{
  visible: boolean
}>()

const menuItems = ref([
  {
    key: 'category',
    label: '分类导航',
    icon: FolderOpened,
  },
])

const activeMenuKey = ref<string | null>(null)
const expandedCategories = ref<number[]>([])
const expandedChildCategories = ref<number[]>([])

onMounted(() => {
  categoryStore.fetchCategories()
})

const toggleMenu = (key: string) => {
  if (activeMenuKey.value === key) {
    activeMenuKey.value = null
  } else {
    activeMenuKey.value = key
  }
}

const toggleCategoryExpand = (categoryId: number) => {
  const index = expandedCategories.value.indexOf(categoryId)
  if (index > -1) {
    expandedCategories.value.splice(index, 1)
  } else {
    expandedCategories.value.push(categoryId)
  }
}

const toggleChildCategoryExpand = (categoryId: number) => {
  const index = expandedChildCategories.value.indexOf(categoryId)
  if (index > -1) {
    expandedChildCategories.value.splice(index, 1)
  } else {
    expandedChildCategories.value.push(categoryId)
  }
}

const handleCategoryClick = (category: CategoryDTO) => {
  router.push(`/category/${category.id}`)
  emit('close')
}
</script>

<style scoped>
.mobile-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.mobile-menu-container {
  width: 100%;
  max-height: 85vh;
  background-color: var(--classic-bg-card);
  border-radius: 16px 16px 0 0;
  display: flex;
  flex-direction: column;
}

.mobile-menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--classic-border);
}

.mobile-menu-title {
  font-size: 16px;
  font-weight: bold;
  color: var(--classic-text-primary);
}

.mobile-menu-close {
  font-size: 20px;
  color: var(--classic-text-secondary);
  cursor: pointer;
}

.mobile-menu-content {
  flex: 1;
  overflow-y: auto;
}

.mobile-menu-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--classic-border);
  cursor: pointer;
  transition: background-color 0.2s;
}

.mobile-menu-item:hover {
  background-color: rgba(139, 115, 85, 0.1);
}

.mobile-menu-item-active {
  background-color: rgba(139, 115, 85, 0.15);
}

.mobile-menu-icon {
  font-size: 18px;
  color: var(--classic-accent-primary);
  margin-right: 12px;
}

.mobile-menu-label {
  flex: 1;
  font-size: 15px;
  color: var(--classic-text-primary);
}

.mobile-menu-arrow {
  font-size: 16px;
  color: var(--classic-accent-primary);
  transition: all 0.25s;
}

.mobile-menu-arrow.rotate {
  transform: rotate(90deg);
}

.mobile-submenu {
  background-color: var(--classic-bg-secondary);
  border-top: 1px solid var(--classic-border);
}

.mobile-category-section {
  padding: 8px 0;
}

.mobile-loading {
  padding: 16px;
  color: var(--classic-text-secondary);
  text-align: center;
}

.mobile-category-item {
  padding: 10px 20px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.mobile-category-item:hover {
  background-color: rgba(139, 115, 85, 0.1);
}

.mobile-category-expanded {
  background-color: rgba(139, 115, 85, 0.08);
}

.mobile-category-header {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}

.mobile-category-icon {
  font-size: 14px;
  color: var(--classic-accent-primary);
  margin-right: 8px;
  flex-shrink: 0;
}

.mobile-category-name {
  flex: 1;
  font-size: 14px;
  font-weight: bold;
  color: var(--classic-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  padding: 4px 6px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.mobile-category-name:hover {
  background-color: rgba(139, 115, 85, 0.1);
}

.mobile-category-expand-btn {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background-color: var(--classic-accent-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  margin-left: 12px;
  box-shadow: 0 2px 8px rgba(139, 115, 85, 0.3);
}

.mobile-category-expand-btn:hover {
  background-color: var(--classic-accent-secondary);
  transform: scale(1.05);
}

.mobile-category-expand-btn:active {
  transform: scale(0.95);
}

.mobile-category-arrow {
  font-size: 18px;
  color: white;
  transition: all 0.25s;
}

.mobile-category-arrow.rotate {
  transform: rotate(180deg);
}

.mobile-category-description {
  font-size: 12px;
  color: var(--classic-text-secondary);
  padding-left: 22px;
  line-height: 1.4;
}

.mobile-category-child {
  padding-left: 36px;
}

.mobile-category-child .mobile-category-description {
  padding-left: 38px;
}

.mobile-category-grandchild {
  padding-left: 52px;
}

.mobile-category-grandchild .mobile-category-description {
  padding-left: 54px;
}

.mobile-category-children {
  margin-top: 4px;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
}

.slide-up-enter-from .mobile-menu-container,
.slide-up-leave-to .mobile-menu-container {
  transform: translateY(100%);
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  max-height: 0;
}

.slide-down-enter-to,
.slide-down-leave-from {
  max-height: 600px;
}
</style>
