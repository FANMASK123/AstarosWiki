<template>
  <div>
    <div class="classic-card-title">用户管理</div>

    <el-table :data="users" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色">
        <template #default="{ row }">
          <el-tag v-if="row.role === 'ADMIN'" type="danger">管理员</el-tag>
          <el-tag v-else>创作者</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'ACTIVE'" type="success">已激活</el-tag>
          <el-tag v-else-if="row.status === 'PENDING'" type="warning">待审核</el-tag>
          <el-tag v-else type="info">已禁用</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button
            size="small"
            v-if="row.status === 'PENDING'"
            type="primary"
            @click="handleApprove(row.id)"
          >
            通过
          </el-button>
          <el-button size="small" v-if="row.status === 'ACTIVE'" @click="handleDisable(row.id)">
            禁用
          </el-button>
          <el-button
            size="small"
            v-if="row.status === 'DISABLED'"
            type="success"
            @click="handleEnable(row.id)"
          >
            启用
          </el-button>
          <el-button
            size="small"
            type="danger"
            v-if="row.role !== 'ADMIN'"
            @click="handleDelete(row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="classic-pagination">
      <el-button :disabled="currentPage === 0" @click="handlePageChange(currentPage - 1)">
        上一页
      </el-button>
      <span style="padding: 8px 16px">第 {{ currentPage + 1 }} / {{ totalPages }} 页</span>
      <el-button
        :disabled="currentPage >= totalPages - 1"
        @click="handlePageChange(currentPage + 1)"
      >
        下一页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getUsers, enableUser, disableUser, deleteUser } from '../../api/admin'
import type { User } from '../../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const users = ref<User[]>([])
const currentPage = ref(0)
const totalPages = ref(0)

onMounted(async () => {
  await loadUsers()
})

const loadUsers = async () => {
  try {
    const res = await getUsers(currentPage.value, 10)
    if (res.code === 200) {
      users.value = res.data.content
      totalPages.value = res.data.totalPages
    }
  } catch (error) {
    console.error('加载用户失败:', error)
  }
}

const handleApprove = async (userId: number) => {
  try {
    await enableUser(userId)
    ElMessage.success('已激活')
    await loadUsers()
  } catch (error) {
    console.error('激活失败:', error)
  }
}

const handleDisable = async (userId: number) => {
  try {
    await ElMessageBox.confirm('确定要禁用这个用户吗？', '确认禁用', {
      confirmButtonText: '禁用',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await disableUser(userId)
    ElMessage.success('已禁用')
    await loadUsers()
  } catch (error) {
    console.error('禁用失败:', error)
  }
}

const handleEnable = async (userId: number) => {
  try {
    await enableUser(userId)
    ElMessage.success('已启用')
    await loadUsers()
  } catch (error) {
    console.error('启用失败:', error)
  }
}

const handleDelete = async (userId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个用户吗？此操作不可恢复。', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await deleteUser(userId)
    ElMessage.success('已删除')
    await loadUsers()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const handlePageChange = async (page: number) => {
  currentPage.value = page
  await loadUsers()
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>
