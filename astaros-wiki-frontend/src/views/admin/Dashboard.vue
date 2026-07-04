<template>
  <div class="dashboard">
    <p class="hw-eyebrow">概览</p>
    <h2 class="hw-section-title">仪表盘</h2>

    <!-- Stat cards -->
    <div class="dash-stats">
      <div class="hw-card dash-stat-card">
        <p class="hw-eyebrow">#1</p>
        <div class="dash-stat-num">{{ stats.userCount }}</div>
        <p class="hw-mono">用户总数</p>
      </div>
      <div class="hw-card dash-stat-card">
        <p class="hw-eyebrow">#2</p>
        <div class="dash-stat-num">{{ stats.pageCount }}</div>
        <p class="hw-mono">页面总数</p>
      </div>
      <div class="hw-card dash-stat-card">
        <p class="hw-eyebrow">#3</p>
        <div class="dash-stat-num">{{ stats.categoryCount }}</div>
        <p class="hw-mono">分类总数</p>
      </div>
      <div class="hw-card dash-stat-card">
        <p class="hw-eyebrow">#4</p>
        <div class="dash-stat-num">{{ stats.tagCount }}</div>
        <p class="hw-mono">标签总数</p>
      </div>
    </div>

    <hr class="hw-divider" />

    <!-- Pending + Logs grid -->
    <div class="dash-grid">
      <div>
        <p class="hw-eyebrow">待处理</p>
        <h3 class="hw-card-title" style="margin-bottom:16px">待审核申请</h3>
        <div v-if="pendingRequests.length === 0" class="hw-mono" style="padding:20px 0">暂无待审核申请</div>
        <div v-else class="dash-list">
          <div v-for="req in pendingRequests" :key="req.id" class="hw-card dash-list-item">
            <div style="flex:1">
              <span class="hw-mono">用户ID: {{ req.userId }}</span>
              <span class="hw-mono" style="margin-left:12px">{{ formatDate(req.createdAt) }}</span>
            </div>
            <div style="display:flex;gap:8px">
              <button class="hw-btn-primary" style="padding:6px 16px;font-size:12px" @click="handleApprove(req.id)">通过</button>
              <button class="hw-btn-ghost" style="padding:6px 16px;font-size:12px" @click="handleReject(req.id)">拒绝</button>
            </div>
          </div>
        </div>
      </div>

      <div>
        <p class="hw-eyebrow">审计</p>
        <h3 class="hw-card-title" style="margin-bottom:16px">最近操作</h3>
        <div v-if="recentLogs.length === 0" class="hw-mono" style="padding:20px 0">暂无操作记录</div>
        <div v-else class="dash-list">
          <div v-for="log in recentLogs" :key="log.id" class="hw-card dash-log-item">
            <span class="hw-mono" style="font-weight:700">{{ log.action }}</span>
            <span class="hw-mono">{{ log.targetType }} #{{ log.targetId }}</span>
            <span class="hw-mono">{{ formatDate(log.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { approveRequest, rejectRequest, getPendingRequests, getOperationLogs } from '../../api/admin'
import type { UserRequest, OperationLog } from '../../api/admin'
import { ElMessage } from 'element-plus'

const stats = ref({ userCount: 0, pageCount: 0, categoryCount: 0, tagCount: 0 })
const pendingRequests = ref<UserRequest[]>([])
const recentLogs = ref<OperationLog[]>([])

onMounted(async () => { await loadPendingRequests(); await loadRecentLogs() })

async function loadPendingRequests() {
  try { const r = await getPendingRequests(0, 5); if (r.code === 200) pendingRequests.value = r.data.content } catch {}
}
async function loadRecentLogs() {
  try { const r = await getOperationLogs(0, 10); if (r.code === 200) recentLogs.value = r.data.content } catch {}
}
async function handleApprove(id: number) {
  try { await approveRequest(id); ElMessage.success('已通过'); await loadPendingRequests() } catch {}
}
async function handleReject(id: number) {
  try { await rejectRequest(id); ElMessage.success('已拒绝'); await loadPendingRequests() } catch {}
}
function formatDate(d: string) { return new Date(d).toLocaleDateString('zh-CN') }
</script>

<style scoped>
.dashboard { max-width: 960px; }
.dash-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 8px; }
.dash-stat-card { text-align: center; padding: 28px 16px; }
.dash-stat-num { font-size: 2.5rem; font-weight: 300; line-height: 1; margin: 8px 0; }
.dash-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 32px; }
.dash-list { display: flex; flex-direction: column; gap: 8px; }
.dash-list-item { display: flex; align-items: center; justify-content: space-between; padding: 16px; }
.dash-log-item { display: flex; flex-direction: column; gap: 4px; padding: 14px; }
@media (max-width: 768px) { .dash-stats, .dash-grid { grid-template-columns: 1fr; } }
</style>
