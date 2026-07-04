<template>
  <div>
    <div class="classic-card-title">管理仪表盘</div>

    <div class="classic-row" style="margin-bottom: 24px">
      <div class="classic-card" style="text-align: center">
        <div style="font-size: 32px; font-weight: bold; color: var(--classic-accent-primary)">
          {{ stats.userCount }}
        </div>
        <div style="color: var(--classic-text-secondary)">用户总数</div>
      </div>
      <div class="classic-card" style="text-align: center">
        <div style="font-size: 32px; font-weight: bold; color: var(--classic-accent-primary)">
          {{ stats.pageCount }}
        </div>
        <div style="color: var(--classic-text-secondary)">页面总数</div>
      </div>
      <div class="classic-card" style="text-align: center">
        <div style="font-size: 32px; font-weight: bold; color: var(--classic-accent-primary)">
          {{ stats.categoryCount }}
        </div>
        <div style="color: var(--classic-text-secondary)">分类总数</div>
      </div>
      <div class="classic-card" style="text-align: center">
        <div style="font-size: 32px; font-weight: bold; color: var(--classic-accent-primary)">
          {{ stats.tagCount }}
        </div>
        <div style="color: var(--classic-text-secondary)">标签总数</div>
      </div>
    </div>

    <div class="classic-row">
      <div class="classic-column">
        <h3
          style="
            color: var(--classic-text-primary);
            border-bottom: 2px solid var(--classic-accent-primary);
            padding-bottom: 8px;
          "
        >
          待审核申请
        </h3>
        <ul class="classic-list">
          <li v-for="request in pendingRequests" :key="request.id" class="classic-list-item">
            <div>
              <strong>用户ID：{{ request.userId }}</strong>
              <div style="color: var(--classic-text-secondary); font-size: 14px">
                {{ formatDate(request.createdAt) }}
              </div>
            </div>
            <div style="margin-top: 8px">
              <el-button size="small" type="primary" @click="handleApprove(request.id)"
                >通过</el-button
              >
              <el-button size="small" @click="handleReject(request.id)" style="margin-left: 8px"
                >拒绝</el-button
              >
            </div>
          </li>
        </ul>
      </div>

      <div class="classic-column">
        <h3
          style="
            color: var(--classic-text-primary);
            border-bottom: 2px solid var(--classic-accent-primary);
            padding-bottom: 8px;
          "
        >
          最近操作
        </h3>
        <ul class="classic-list">
          <li v-for="log in recentLogs" :key="log.id" class="classic-list-item">
            <div>
              <strong>{{ log.action }}</strong>
              <div style="color: var(--classic-text-secondary); font-size: 14px">
                {{ log.targetType }} - {{ log.targetId }} | {{ formatDate(log.createdAt) }}
              </div>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  approveRequest,
  rejectRequest,
  getPendingRequests,
  getOperationLogs,
} from '../../api/admin'
import type { UserRequest, OperationLog } from '../../api/admin'
import { ElMessage } from 'element-plus'

const stats = ref({
  userCount: 0,
  pageCount: 0,
  categoryCount: 0,
  tagCount: 0,
})

const pendingRequests = ref<UserRequest[]>([])
const recentLogs = ref<OperationLog[]>([])

onMounted(async () => {
  await loadPendingRequests()
  await loadRecentLogs()
})

const loadPendingRequests = async () => {
  try {
    const res = await getPendingRequests(0, 5)
    if (res.code === 200) {
      pendingRequests.value = res.data.content
    }
  } catch (error) {
    console.error('加载待审核申请失败:', error)
  }
}

const loadRecentLogs = async () => {
  try {
    const res = await getOperationLogs(0, 10)
    if (res.code === 200) {
      recentLogs.value = res.data.content
    }
  } catch (error) {
    console.error('加载操作日志失败:', error)
  }
}

const handleApprove = async (requestId: number) => {
  try {
    const res = await approveRequest(requestId)
    if (res.code === 200) {
      ElMessage.success('审批通过')
      await loadPendingRequests()
    }
  } catch (error) {
    console.error('审批失败:', error)
  }
}

const handleReject = async (requestId: number) => {
  try {
    const res = await rejectRequest(requestId)
    if (res.code === 200) {
      ElMessage.success('已拒绝')
      await loadPendingRequests()
    }
  } catch (error) {
    console.error('拒绝失败:', error)
  }
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN')
}
</script>
