<template>
  <div>
    <div class="hw-eyebrow">操作日志</div>

    <el-table :data="logs" style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="action" label="操作" />
      <el-table-column prop="targetType" label="目标类型" />
      <el-table-column prop="targetId" label="目标ID" />
      <el-table-column prop="detail" label="详情">
        <template #default="{ row }">
          <el-popover placement="top" :width="400" trigger="hover" :content="row.detail">
            <template #reference>
              <el-button size="small">查看详情</el-button>
            </template>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column prop="ipAddress" label="IP地址" />
      <el-table-column prop="createdAt" label="时间">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
    </el-table>

    <div class="hw-pagination">
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
import { getOperationLogs } from '../../api/admin'
import type { OperationLog } from '../../api/admin'

const logs = ref<OperationLog[]>([])
const currentPage = ref(0)
const totalPages = ref(0)

onMounted(async () => {
  await loadLogs()
})

const loadLogs = async () => {
  try {
    const res = await getOperationLogs(currentPage.value, 20)
    if (res.code === 200) {
      logs.value = res.data.content
      totalPages.value = res.data.totalPages
    }
  } catch (error) {
    console.error('加载日志失败:', error)
  }
}

const handlePageChange = async (page: number) => {
  currentPage.value = page
  await loadLogs()
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}
</script>
