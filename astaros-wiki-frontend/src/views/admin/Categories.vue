<template>
  <div>
    <div class="classic-card-title">分类管理</div>

    <el-button type="primary" :icon="Plus" @click="handleCreate" style="margin-bottom: 16px">
      新建分类
    </el-button>

    <el-tree
      :data="categoryTree"
      :props="{ children: 'children', label: 'name' }"
      node-key="id"
      default-expand-all
    >
      <template #default="{ data }">
        <div
          style="display: flex; justify-content: space-between; align-items: center; width: 100%"
        >
          <div>
            <span style="font-weight: bold">{{ data.name }}</span>
            <span style="color: var(--classic-text-secondary); font-size: 14px; margin-left: 8px">
              {{ data.description || '暂无描述' }}
            </span>
          </div>
          <div>
            <el-button size="small" :icon="Edit" @click="handleEdit(data)">编辑</el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(data.id)"
              >删除</el-button
            >
          </div>
        </div>
      </template>
    </el-tree>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editMode ? '编辑分类' : '新建分类'" width="500px">
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="categoryForm.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="categoryForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="父分类">
          <el-tree-select
            v-model="categoryForm.parentId"
            :data="categoryTree"
            :props="{ value: 'id', label: 'name', children: 'children' }"
            placeholder="请选择父分类"
            clearable
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useCategoryStore } from '../../stores/category'
import { createCategory, updateCategory, deleteCategory } from '../../api/category'
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

onMounted(async () => {
  await categoryStore.fetchCategories()
})

const handleCreate = () => {
  editMode.value = false
  editingCategoryId.value = null
  categoryForm.value = {
    name: '',
    description: '',
    parentId: null,
    sortOrder: 0,
  }
  dialogVisible.value = true
}

const handleEdit = (category: CategoryDTO) => {
  editMode.value = true
  editingCategoryId.value = category.id
  categoryForm.value = {
    name: category.name,
    description: category.description || '',
    parentId: category.parentId || null,
    sortOrder: category.sortOrder,
  }
  dialogVisible.value = true
}

const handleSave = async () => {
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
    }
  } catch (error) {
    console.error('保存失败:', error)
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个分类吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const res = await deleteCategory(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await categoryStore.fetchCategories()
    }
  } catch (error) {
    console.error('删除失败:', error)
  }
}
</script>
