<template>
  <div class="hw-card">
    <div class="hw-eyebrow">页面历史版本</div>

    <div v-if="loading">加载中...</div>
    <div v-else-if="versions.length === 0">
      <el-empty description="暂无历史版本" />
    </div>
    <ul v-else class="hw-list">
      <li v-for="version in versions" :key="version.id" class="hw-list-item">
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div>
            <div style="font-weight: bold">版本 {{ version.versionNumber }}</div>
            <div style="color: var(--var(--color-muted)); font-size: 14px">
              编辑者：{{ version.editorId }} | {{ formatDate(version.createdAt) }}
            </div>
          </div>
          <div>
            <el-button size="small" @click="handleViewVersion(version)">查看</el-button>
            <el-button
              size="small"
              type="primary"
              @click="handleRollback(version.versionNumber)"
              :disabled="!userStore.isLoggedIn()"
            >
              回滚
            </el-button>
          </div>
        </div>
      </li>
    </ul>

    <el-dialog v-model="versionDialogVisible" title="版本内容" width="80%">
      <div class="hw-card-content" style="white-space: pre-wrap">
        {{ currentVersionContent }}
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

const route = useRoute()
const userStore = useUserStore()
const versions = ref<PageVersion[]>([])
const loading = ref(true)
const versionDialogVisible = ref(false)
const currentVersionContent = ref('')
const slug = route.params.slug as string

onMounted(async () => {
  await loadHistory()
})

const loadHistory = async () => {
  loading.value = true
  try {
    const res = await getPageHistory(slug)
    if (res.code === 200) {
      versions.value = res.data
    }
  } catch (error) {
    console.error('获取历史版本失败:', error)
  } finally {
    loading.value = false
  }
}

const handleViewVersion = (version: PageVersion) => {
  currentVersionContent.value = version.content
  versionDialogVisible.value = true
}

const handleRollback = async (versionNumber: number) => {
  try {
    await ElMessageBox.confirm(`确定要回滚到版本 ${versionNumber} 吗？`, '确认回滚', {
      confirmButtonText: '回滚',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const res = await rollbackPage(slug, versionNumber)
    if (res.code === 200) {
      ElMessage.success('回滚成功')
      await loadHistory()
    }
  } catch (error) {
    console.error('回滚失败:', error)
  }
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
