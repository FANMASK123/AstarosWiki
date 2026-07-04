<template>
  <div>
    <p class="eyebrow">管理后台</p>
    <div class="card mt-16">
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
    <div class="card mt-16">
      <div class="card-title">最近日志</div>
      <div v-if="loading2" class="loading"><div class="spinner"></div></div>
      <div v-else-if="recentLogs.length===0"><p class="text-muted">暂无日志</p></div>
      <div v-else class="stack mt-16">
        <div v-for="l in recentLogs" :key="l.id" class="card">
          <strong>{{ l.action }}</strong><br>
          <span class="mono">{{ l.targetType }} #{{ l.targetId }} · {{ fmt(l.createdAt) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPendingRequests, getOperationLogs, approveRequest, rejectRequest } from '../../api/admin'
import type { UserRequest, OperationLog } from '../../api/admin'
import { ElMessage } from 'element-plus'

const pendingRequests = ref<UserRequest[]>([]); const recentLogs = ref<OperationLog[]>([])
const loading1 = ref(true); const loading2 = ref(true)

onMounted(async () => { await loadPendingRequests(); await loadRecentLogs() })
async function loadPendingRequests() { try { const r = await getPendingRequests(0,5); if (r.code===200) pendingRequests.value=r.data.content } catch(e) { console.error(e) } finally { loading1.value=false } }
async function loadRecentLogs() { try { const r = await getOperationLogs(0,10); if (r.code===200) recentLogs.value=r.data.content } catch(e) { console.error(e) } finally { loading2.value=false } }
async function handleApprove(id: number) { try { await approveRequest(id); ElMessage.success('已通过'); await loadPendingRequests() } catch(e) { console.error(e) } }
async function handleReject(id: number) { try { await rejectRequest(id); ElMessage.success('已拒绝'); await loadPendingRequests() } catch(e) { console.error(e) } }
function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN') }
</script>
