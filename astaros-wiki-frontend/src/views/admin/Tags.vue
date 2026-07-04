<template>
  <div>
    <div class="hw-eyebrow">标签管理</div>

    <el-input
      v-model="newTagName"
      placeholder="输入新标签名称"
      style="width: 300px; margin-bottom: 16px"
    >
      <template #append>
        <el-button :icon="Plus" @click="handleCreate">添加</el-button>
      </template>
    </el-input>

    <el-table :data="tags" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="标签名" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAllTags, createTag, deleteTag } from '../../api/tag'
import type { Tag } from '../../api/tag'
import { Plus, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const tags = ref<Tag[]>([])
const newTagName = ref('')

onMounted(async () => {
  await loadTags()
})

const loadTags = async () => {
  try {
    const res = await getAllTags()
    if (res.code === 200) {
      tags.value = res.data
    }
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

const handleCreate = async () => {
  if (!newTagName.value.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }

  try {
    const res = await createTag(newTagName.value.trim())
    if (res.code === 200) {
      ElMessage.success('创建成功')
      newTagName.value = ''
      await loadTags()
    }
  } catch (error) {
    console.error('创建失败:', error)
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个标签吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const res = await deleteTag(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadTags()
    }
  } catch (error) {
    console.error('删除失败:', error)
  }
}
</script>
