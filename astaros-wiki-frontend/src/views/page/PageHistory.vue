<template>
  <div>
    <div class="card">
      <p class="eyebrow">页面历史版本</p>
      <div v-if="loading" class="loading"><div class="spinner"></div></div>
      <div v-else-if="versions.length===0" class="empty"><p class="text-muted">暂无历史版本</p></div>
      <div v-else class="stack mt-16">
        <div v-for="v in versions" :key="v.id" class="card" style="display:flex;justify-content:space-between;align-items:center">
          <div><strong>版本 {{ v.versionNumber }}</strong><br><span class="mono">编辑者：{{ v.editorId }} | {{ fmt(v.createdAt) }}</span></div>
          <div class="row">
            <button class="btn btn-ghost" style="font-size:11px;padding:6px 12px" @click="view(v)">查看</button>
            <button class="btn btn-primary" style="font-size:11px;padding:6px 12px" :disabled="!userStore.isLoggedIn()" @click="rollback(v.versionNumber)">回滚</button>
          </div>
        </div>
      </div>
    </div>
    <el-dialog v-model="dialogVisible" title="版本内容" width="80%"><div style="white-space:pre-wrap">{{ dialogContent }}</div></el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { getPageHistory, rollbackPage } from '../../api/page'
import type { PageVersion } from '../../api/page'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute(); const userStore = useUserStore()
const versions = ref<PageVersion[]>([]); const loading = ref(true)
const dialogVisible = ref(false); const dialogContent = ref('')
const slug = route.params.slug as string

onMounted(async () => { try { const r = await getPageHistory(slug); if (r.code===200) versions.value=r.data } catch(e) { console.error(e) } finally { loading.value=false } })
function view(v: PageVersion) { dialogContent.value=v.content; dialogVisible.value=true }
async function rollback(vn: number) {
  try { await ElMessageBox.confirm(`回滚到版本 ${vn}？`,'确认',{type:'warning'}); const r = await rollbackPage(slug,vn); if (r.code===200) { ElMessage.success('已回滚'); window.location.reload() } } catch(e) { console.error(e) }
}
function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN',{year:'numeric',month:'short',day:'numeric'}) }
</script>
