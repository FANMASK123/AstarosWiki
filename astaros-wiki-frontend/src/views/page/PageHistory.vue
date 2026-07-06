<template>
  <div>
    <div class="card">
      <p class="eyebrow">页面历史版本</p>
      <div v-if="loading" class="loading"><div class="spinner"></div></div>
      <div v-else-if="versions.length===0" class="empty"><p class="text-muted">暂无历史版本</p></div>
      <div v-else class="stack mt-16">
        <div v-for="v in versions" :key="v.id" class="card" :style="{ borderColor: v.versionNumber===currentPageVersion ? 'var(--fg)' : '' }">
          <div class="row" style="justify-content:space-between">
            <div>
              <strong>版本 {{ v.versionNumber }}</strong>
              <span v-if="v.versionNumber===currentPageVersion" class="tag" style="margin-left:8px">当前</span>
              <br><span class="mono">编辑者：{{ v.editorId }} | {{ fmt(v.createdAt) }}</span>
            </div>
            <div class="row">
              <button class="btn btn-ghost" style="font-size:11px;padding:6px 12px" @click="view(v)">查看</button>
              <button class="btn btn-ghost" style="font-size:11px;padding:6px 12px" @click="selectForDiff(v)">对比</button>
              <button class="btn btn-primary" style="font-size:11px;padding:6px 12px" :disabled="!userStore.isLoggedIn()" @click="rollback(v.versionNumber)">回滚</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Version content viewer -->
    <el-dialog v-model="dialogVisible" title="版本内容" width="80%">
      <div style="white-space:pre-wrap;font-family:var(--mono);font-size:13px;line-height:1.6">{{ dialogContent }}</div>
    </el-dialog>

    <!-- Diff viewer -->
    <el-dialog v-model="diffVisible" title="版本对比" width="80%">
      <div class="diff-header">
        <span>版本 <strong>{{ diffFrom?.versionNumber }}</strong> → 版本 <strong>{{ diffTo?.versionNumber }}</strong></span>
        <button class="btn btn-ghost btn-sm" @click="diffVisible=false">关闭</button>
      </div>
      <div class="diff-body">
        <div v-if="diffLines.length===0" class="text-muted">内容完全相同，无差异。</div>
        <pre v-for="(line, i) in diffLines" :key="i" :class="['diff-line', line.type]">{{ line.text }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { getPageHistory, rollbackPage } from '../../api/page'
import type { PageVersion } from '../../api/page'
import { ElMessage, ElMessageBox } from 'element-plus'
import { diffLines as computeDiffLines } from 'diff'

const route = useRoute(); const userStore = useUserStore()
const versions = ref<PageVersion[]>([]); const loading = ref(true)
const dialogVisible = ref(false); const dialogContent = ref('')
const slug = route.params.slug as string
const currentPageVersion = ref(0)

// Diff state
const diffVisible = ref(false)
const diffFrom = ref<PageVersion|null>(null)
const diffTo = ref<PageVersion|null>(null)
const diffLines = ref<{text:string,type:'added'|'removed'|'unchanged'}[]>([])

onMounted(async () => {
  try {
    const r = await getPageHistory(slug)
    if (r.code===200) {
      versions.value = r.data
      // Find current version
      const cur = r.data.find((v: PageVersion) => v.versionNumber === currentPageVersion.value)
      if (!cur && r.data.length > 0) {
        // Get current page version from page data
      }
    }
  } catch(e) { console.error(e) } finally { loading.value=false }
})

function view(v: PageVersion) {
  dialogContent.value = v.content
  dialogVisible.value = true
}

function selectForDiff(v: PageVersion) {
  if (!diffFrom.value) {
    diffFrom.value = v
    ElMessage.info(`已选择版本 ${v.versionNumber} 作为对比起点，请再选择一个版本`)
    return
  }
  if (diffFrom.value.versionNumber === v.versionNumber) {
    ElMessage.warning('请选择不同的版本')
    return
  }
  // Determine order: older → newer
  const from = diffFrom.value.versionNumber < v.versionNumber ? diffFrom.value : v
  const to = diffFrom.value.versionNumber < v.versionNumber ? v : diffFrom.value
  diffFrom.value = from
  diffTo.value = to
  computeDiff()
  diffVisible.value = true
  diffFrom.value = null // reset for next time
}

function computeDiff() {
  if (!diffFrom.value || !diffTo.value) return
  const changes = computeDiffLines(diffFrom.value.content, diffTo.value.content)
  diffLines.value = changes.map((c: { added?: boolean; removed?: boolean; value: string }) => {
    const lines = c.value.split('\n')
    // Remove last empty element if content ends with newline
    if (lines.length > 1 && lines[lines.length - 1] === '') lines.pop()
    return lines.map((text: string) => ({
      text: text || ' ',
      type: c.added ? 'added' as const : c.removed ? 'removed' as const : 'unchanged' as const
    }))
  }).flat()
}

async function rollback(vn: number) {
  try {
    await ElMessageBox.confirm(`回滚到版本 ${vn}？`, '确认', { type: 'warning' })
    const r = await rollbackPage(slug, vn)
    if (r.code===200) { ElMessage.success('已回滚'); window.location.reload() }
  } catch(e) { console.error(e) }
}

function fmt(d: string) {
  return new Date(d).toLocaleDateString('zh-CN', { year:'numeric', month:'short', day:'numeric', hour:'2-digit', minute:'2-digit' })
}
</script>

<style scoped>
.diff-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}

.diff-body {
  max-height: 60vh;
  overflow-y: auto;
  background: color-mix(in srgb, var(--fg), 3%);
  border-radius: 4px;
  padding: 12px;
}

.diff-line {
  font-family: var(--mono);
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
  padding: 2px 8px;
  white-space: pre-wrap;
  word-break: break-all;
}

.diff-line.added {
  background: rgba(34, 197, 94, 0.12);
  color: rgb(134, 239, 172);
}

.diff-line.removed {
  background: rgba(239, 68, 68, 0.12);
  color: rgb(252, 165, 165);
}

.diff-line.unchanged {
  color: var(--muted);
}

.btn-sm {
  font-size: 11px;
  padding: 4px 10px;
}
</style>
