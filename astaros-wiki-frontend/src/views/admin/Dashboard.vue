<template>
  <div>
    <p class="eyebrow">管理后台</p>
    <h1 class="section-title">仪表盘</h1>

    <!-- Stats cards -->
    <div class="dash-stats">
      <div class="card dash-stat">
        <p class="eyebrow" style="font-size:9px">用户</p>
        <div class="dash-num">{{ stats.userCount }}</div>
        <p class="mono" style="font-size:11px">总用户数</p>
      </div>
      <div class="card dash-stat">
        <p class="eyebrow" style="font-size:9px">页面</p>
        <div class="dash-num">{{ stats.pageCount }}</div>
        <p class="mono" style="font-size:11px">总页面数</p>
      </div>
      <div class="card dash-stat">
        <p class="eyebrow" style="font-size:9px">分类</p>
        <div class="dash-num">{{ stats.categoryCount }}</div>
        <p class="mono" style="font-size:11px">分类数</p>
      </div>
      <div class="card dash-stat">
        <p class="eyebrow" style="font-size:9px">待审核</p>
        <div class="dash-num" :style="{ color: stats.pendingCount > 0 ? '#f59e0b' : '' }">{{ stats.pendingCount }}</div>
        <p class="mono" style="font-size:11px">待审核申请</p>
      </div>
    </div>

    <!-- Two column: pending requests + recent logs -->
    <div class="dash-grid">
      <div class="card">
        <div class="card-title">申请审核</div>
        <div v-if="loading1" class="loading"><div class="spinner"></div></div>
        <div v-else-if="pendingRequests.length===0"><p class="text-muted">暂无待审核申请</p></div>
        <div v-else class="stack mt-16">
          <div v-for="r in pendingRequests" :key="r.id" class="card" style="display:flex;justify-content:space-between;align-items:center">
            <div><strong>用户 #{{ r.userId }}</strong><br><span class="mono">{{ r.requestType }} · {{ fmt(r.createdAt) }}</span></div>
            <div class="row">
              <button class="btn btn-primary" style="font-size:11px;padding:5px 12px" @click="handleApprove(r.id)">通过</button>
              <button class="btn btn-ghost" style="font-size:11px;padding:5px 12px" @click="handleReject(r.id)">拒绝</button>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-title">最近日志</div>
        <div v-if="loading2" class="loading"><div class="spinner"></div></div>
        <div v-else-if="recentLogs.length===0"><p class="text-muted">暂无日志</p></div>
        <div v-else class="stack mt-16" style="max-height:400px;overflow-y:auto">
          <div v-for="l in recentLogs" :key="l.id" class="card">
            <strong>{{ l.action }}</strong><br>
            <span class="mono">{{ l.targetType }} #{{ l.targetId }} · {{ fmt(l.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getPendingRequests, getOperationLogs, approveRequest, rejectRequest } from '../../api/admin'
import { getUsers } from '../../api/admin'
import { getPublishedPages } from '../../api/page'
import { useCategoryStore } from '../../stores/category'
import type { UserRequest, OperationLog } from '../../api/admin'
import { ElMessage } from 'element-plus'

const pendingRequests = ref<UserRequest[]>([]); const recentLogs = ref<OperationLog[]>([])
const loading1 = ref(true); const loading2 = ref(true)
const categoryStore = useCategoryStore()

const stats = reactive({ userCount: 0, pageCount: 0, categoryCount: 0, pendingCount: 0 })

onMounted(async () => {
  await Promise.all([loadPendingRequests(), loadRecentLogs(), loadStats(), categoryStore.fetchCategories()])
  stats.categoryCount = categoryStore.categories.length
})

async function loadStats() {
  try {
    const [users, pages, pending] = await Promise.all([
      getUsers(0, 1),
      getPublishedPages(0, 1),
      getPendingRequests(0, 1),
    ])
    if (users.code === 200) stats.userCount = users.data.total
    if (pages.code === 200) stats.pageCount = pages.data.total
    if (pending.code === 200) stats.pendingCount = pending.data.total
  } catch (e) { console.error(e) }
}

async function loadPendingRequests() {
  try { const r = await getPendingRequests(0, 5); if (r.code === 200) pendingRequests.value = r.data.content }
  catch (e) { console.error(e) } finally { loading1.value = false }
}

async function loadRecentLogs() {
  try { const r = await getOperationLogs(0, 10); if (r.code === 200) recentLogs.value = r.data.content }
  catch (e) { console.error(e) } finally { loading2.value = false }
}

async function handleApprove(id: number) {
  try { await approveRequest(id); ElMessage.success('已通过'); await loadPendingRequests(); await loadStats() }
  catch (e) { console.error(e) }
}

async function handleReject(id: number) {
  try { await rejectRequest(id); ElMessage.success('已拒绝'); await loadPendingRequests(); await loadStats() }
  catch (e) { console.error(e) }
}

function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN') }
</script>

<style scoped>
.dash-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}
.dash-stat {
  text-align: center;
  padding: 24px 16px;
}
.dash-num {
  font-size: 32px;
  font-weight: 700;
  font-family: var(--mono);
  color: var(--fg);
  margin: 8px 0 4px;
}
.dash-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
@media(max-width: 768px) {
  .dash-stats { grid-template-columns: repeat(2, 1fr); }
  .dash-grid { grid-template-columns: 1fr; }
}
</style>
