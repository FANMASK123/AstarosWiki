<template>
  <div class="admin-categories">
    <p class="eyebrow">分类管理</p>
    <h1 class="section-title">分类管理</h1>

    <button class="btn btn-primary" @click="handleCreate" style="margin-bottom: 16px">
      <Plus style="margin-right: 6px" />新建分类
    </button>

    <div class="card cat-tree-card">
      <el-tree
        :data="categoryTree"
        :props="{ children: 'children', label: 'name' }"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
      >
        <template #default="{ data, node }">
          <div class="cat-tree-row">
            <div class="cat-tree-info" @click="handleTreeClick(data)">
              <span class="cat-tree-name">{{ data.name }}</span>
              <span v-if="data.description" class="cat-tree-desc">{{ data.description }}</span>
            </div>
            <div class="cat-tree-actions">
              <el-tooltip
                v-if="node.level >= 3"
                content="已达最大深度（3级）"
                placement="top"
              >
                <span style="display:inline-block">
                  <button class="btn btn-ghost btn-sm" disabled>
                    <Plus :size="14" />
                  </button>
                </span>
              </el-tooltip>
              <button
                v-else
                class="btn btn-ghost btn-sm"
                @click="handleAddChild(data)"
              >
                <Plus :size="14" />
              </button>
              <button class="btn btn-ghost btn-sm" @click="handleEdit(data)">
                <Edit :size="14" />
              </button>
              <button class="btn btn-danger btn-sm" @click="handleDelete(data)">
                <Delete :size="14" />
              </button>
            </div>
          </div>
        </template>
      </el-tree>
    </div>

    <el-dialog v-model="dialogVisible" :title="editMode ? '编辑分类' : '新建分类'" width="500px">
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="categoryForm.description" type="textarea" :rows="3" placeholder="请输入分类描述" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-tree-select
            v-model="categoryForm.parentId"
            :data="availableParents"
            :props="{ value: 'id', label: 'name', children: 'children' }"
            placeholder="请选择父分类（可选）"
            clearable
            :disabled="isThirdLevel"
            check-strictly
          />
          <span v-if="isThirdLevel" style="color: var(--muted); font-size: 12px; display: block; margin-top: 4px">
            分类最多支持三级，无法再添加子分类
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <button class="btn btn-ghost" @click="dialogVisible = false">取消</button>
        <button class="btn btn-primary" @click="handleSave">保存</button>
      </template>
    </el-dialog>

    <!-- Migration dialog -->
    <el-dialog v-model="migrationVisible" title="删除分类 — 页面迁移" width="500px" @closed="migrateToId=null; migratePageCount=0; deleteTarget=null">
      <p style="margin-bottom:16px;color:var(--muted)">
        分类「<strong style="color:var(--fg)">{{ deleteTarget?.name }}</strong>」下有 <strong style="color:var(--fg)">{{ migratePageCount }}</strong> 个页面，请选择迁移目标：
      </p>
      <el-select v-model="migrateToId" placeholder="选择目标分类" style="width:100%">
        <el-option v-for="t in migrateTargets" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <template #footer>
        <button class="btn btn-ghost" @click="migrationVisible = false">取消</button>
        <button class="btn btn-primary" @click="handleMigrate">迁移并删除</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useCategoryStore } from '../../stores/category'
import { createCategory, updateCategory, deleteCategory } from '../../api/category'
import { getPagesByCategory } from '../../api/page'
import type { CategoryDTO, CategoryCreateRequest } from '../../api/category'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const categoryStore = useCategoryStore()
const categoryTree = computed(() => categoryStore.categories)
const dialogVisible = ref(false)
const editMode = ref(false)
const categoryForm = ref<CategoryCreateRequest>({
  name: '',
  description: '',
  parentId: null,
  sortOrder: 0,
})
const editingCategoryId = ref<number | null>(null)
const parentCategory = ref<CategoryDTO | null>(null)

const isThirdLevel = computed(() => {
  if (!parentCategory.value) return false
  return getCategoryLevel(parentCategory.value) >= 3
})

// Filter tree for parent selector: only depth ≤ 2 nodes can be parents
const availableParents = computed((): CategoryDTO[] => {
  function cloneShallow(nodes: CategoryDTO[], depth: number): CategoryDTO[] {
    return nodes
      .filter((n: CategoryDTO): boolean => depth < 3 && !!n)
      .map((n: CategoryDTO) => ({
        ...n,
        children: n.children?.length ? cloneShallow(n.children, depth + 1) : [],
      }))
  }
  return cloneShallow(categoryStore.categories, 1)
})

function getCategoryLevel(category: CategoryDTO): number {
  let level = 1
  let parentId = category.parentId
  while (parentId) {
    const parent = findCategoryById(parentId)
    if (!parent) break
    level++
    parentId = parent.parentId
  }
  return level
}

function findCategoryById(id: number): CategoryDTO | null {
  function search(nodes: CategoryDTO[]): CategoryDTO | null {
    for (const node of nodes) {
      if (node.id === id) return node
      if (node.children?.length) {
        const found = search(node.children)
        if (found) return found
      }
    }
    return null
  }
  return search(categoryStore.categories)
}

onMounted(async () => {
  await categoryStore.fetchCategories()
})

const handleCreate = () => {
  editMode.value = false
  editingCategoryId.value = null
  parentCategory.value = null
  categoryForm.value = {
    name: '',
    description: '',
    parentId: null,
    sortOrder: 0,
  }
  dialogVisible.value = true
}

const handleAddChild = (parent: CategoryDTO) => {
  if (getCategoryLevel(parent) >= 3) {
    ElMessage.warning('分类最多支持三级，无法再添加子分类')
    return
  }
  editMode.value = false
  editingCategoryId.value = null
  parentCategory.value = parent
  categoryForm.value = {
    name: '',
    description: '',
    parentId: parent.id,
    sortOrder: 0,
  }
  dialogVisible.value = true
}

const handleEdit = (category: CategoryDTO) => {
  editMode.value = true
  editingCategoryId.value = category.id
  parentCategory.value = category.parentId ? findCategoryById(category.parentId) || null : null
  categoryForm.value = {
    name: category.name,
    description: category.description || '',
    parentId: category.parentId || null,
    sortOrder: category.sortOrder,
  }
  dialogVisible.value = true
}

const handleTreeClick = (_data: CategoryDTO) => {
}

const handleSave = async () => {
  if (!categoryForm.value.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }

  try {
    let res
    if (editMode.value && editingCategoryId.value) {
      res = await updateCategory(editingCategoryId.value, categoryForm.value)
    } else {
      res = await createCategory(categoryForm.value)
    }

    if (res.code === 200) {
      ElMessage.success(editMode.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      await categoryStore.fetchCategories()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error: any) {
    console.error('保存失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败')
  }
}

const handleDelete = async (category: CategoryDTO) => {
  try {
    // First confirm
    await ElMessageBox.confirm(`确定要删除分类"${category.name}"吗？`, '确认删除', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning',
    })

    // Check if category has children
    if (category.children?.length) {
      ElMessage.warning('该分类下有子分类，请先删除子分类')
      return
    }

    // Check if category has pages
    let hasPages = false
    let pageCount = 0
    try {
      const pr = await getPagesByCategory(category.id, 0, 1)
      hasPages = pr.code === 200 && pr.data.total > 0
      pageCount = pr.code === 200 ? pr.data.total : 0
    } catch { /* ignore */ }

    if (hasPages) {
      deleteTarget.value = category
      migratePageCount.value = pageCount
      migrationVisible.value = true
      return
    }

    const res = await deleteCategory(category.id)
    if (res.code === 200) { ElMessage.success('删除成功'); await categoryStore.fetchCategories() }
    else ElMessage.error(res.msg || '删除失败')
  } catch (error: any) {
    if (error !== 'cancel') { console.error('删除失败:', error); ElMessage.error(error.response?.data?.message || '删除失败') }
  }
}

// Migration dialog
const deleteTarget = ref<CategoryDTO | null>(null)
const migrationVisible = ref(false)
const migrateToId = ref<number | null>(null)
const migratePageCount = ref(0)

async function handleMigrate() {
  if (!deleteTarget.value || !migrateToId.value) { ElMessage.warning('请选择目标分类'); return }
  try {
    const res = await deleteCategory(deleteTarget.value.id, migrateToId.value)
    if (res.code === 200) {
      ElMessage.success(`已将"${deleteTarget.value.name}"下的页面迁移至目标分类并删除`)
      migrationVisible.value = false; deleteTarget.value = null; migrateToId.value = null
      await categoryStore.fetchCategories()
    } else ElMessage.error(res.msg || '操作失败')
  } catch (e: any) { console.error(e); ElMessage.error('操作失败') }
}

// Filter categories for migration (exclude self + descendants)
const migrateTargets = computed((): CategoryDTO[] => {
  const results: CategoryDTO[] = []
  function walk(nodes: CategoryDTO[], skipId: number) {
    for (const n of nodes) {
      if (n.id !== skipId) { results.push(n); if (n.children?.length) walk(n.children, skipId) }
    }
  }
  walk(categoryStore.categories, deleteTarget.value?.id || 0)
  return results
})
</script>

<style scoped>
.cat-tree-card {
  max-height: 600px;
  overflow-y: auto;
}

.cat-tree-card :deep(.el-tree) {
  background: transparent;
}

.cat-tree-card :deep(.el-tree-node__content) {
  height: auto;
  min-height: 36px;
}

.admin-categories { max-width: 900px; }

.cat-tree-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 4px 0;
}

.cat-tree-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.cat-tree-name {
  font-weight: 500;
  color: var(--fg);
}

.cat-tree-desc {
  color: var(--muted);
  font-size: 12px;
}

.cat-tree-actions {
  display: flex;
  gap: 4px;
}

.btn-sm {
  padding: 4px 8px;
  font-size: 12px;
}

.btn-danger {
  color: #ef4444;
  border: 1px solid #ef4444;
  background: transparent;
}
.btn-danger:hover {
  background: rgba(239, 68, 68, 0.12);
  border-color: #dc2626;
}
</style>
