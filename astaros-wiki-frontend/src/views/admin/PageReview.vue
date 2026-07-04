<template>
  <div v-if="loading" class="hw-card">加载中...</div>
  <div v-else-if="page" class="review-page">
    <div class="review-page-header">
      <router-link to="/admin/pages" class="hw-nav-link" style="margin-right: 16px">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </router-link>
      <h2 class="review-page-title">{{ page.title }}</h2>
      <el-tag :type="getStatusTagType(page.status)" style="margin-left: 16px">{{
        getStatusText(page.status)
      }}</el-tag>
    </div>

    <div class="review-content">
      <div class="review-content-left">
        <div class="hw-card">
          <div style="margin-bottom: 16px">
            <span class="hw-tag" v-if="page.categoryName">{{ page.categoryName }}</span>
            <span class="hw-tag" v-for="tag in page.tags" :key="tag">{{ tag }}</span>
          </div>

          <div style="color: var(--var(--color-muted)); font-size: 14px; margin-bottom: 24px">
            作者：{{ page.authorName }} | 版本：{{ page.version }} | 浏览：{{ page.viewCount }}次 |
            创建时间：{{ formatDate(page.createdAt) }} | 更新时间：{{ formatDate(page.updatedAt) }}
          </div>

          <div class="document-scroll">
            <div class="document-content">
              {{ page.draftContent || page.content }}
            </div>
          </div>

          <div
            v-if="page.draftStatus"
            style="
              margin-top: 16px;
              padding: 12px;
              background-color: var(--color-mix(in srgb, var(--color-foreground) 4%, transparent));
              border-radius: 8px;
            "
          >
            <div
              style="font-weight: bold; color: var(--var(--color-foreground)); margin-bottom: 4px"
            >
              草稿状态
            </div>
            <div style="color: var(--var(--color-muted))">
              {{ page.draftStatus === 'SAVED' ? '已保存（待提交审核）' : '待审核' }}
            </div>
            <div v-if="page.rejectReason" style="color: #f56c6c; margin-top: 8px">
              驳回原因：{{ page.rejectReason }}
            </div>
          </div>
        </div>
      </div>

      <div class="review-content-right">
        <div class="hw-card review-panel">
          <h3 class="review-panel-title">{{ isPublished ? '管理操作' : '审核操作' }}</h3>

          <div v-if="!isPublished" style="margin-bottom: 16px">
            <div style="font-size: 13px; color: var(--var(--color-muted)); margin-bottom: 8px">
              审核意见（将发送给创作者）
            </div>
            <textarea
              v-model="reviewForm.opinion"
              rows="6"
              placeholder="请输入审核意见或修改建议..."
              maxlength="5000"
              style="
                width: 100%;
                padding: 12px;
                border: 1px solid #dcdfe6;
                border-radius: 4px;
                resize: vertical;
                font-size: 14px;
                box-sizing: border-box;
                min-height: 120px;
              "
            />
            <div style="text-align: right; color: #909399; font-size: 12px; margin-top: 4px">
              {{ reviewForm.opinion.length }}/5000
            </div>
          </div>

          <div v-if="isPublished" style="margin-bottom: 16px">
            <div style="font-size: 13px; color: var(--var(--color-muted)); margin-bottom: 8px">
              修改建议（将发送给创作者）
            </div>
            <textarea
              v-model="reviewForm.opinion"
              rows="6"
              placeholder="请输入修改建议或提醒..."
              maxlength="5000"
              style="
                width: 100%;
                padding: 12px;
                border: 1px solid #dcdfe6;
                border-radius: 4px;
                resize: vertical;
                font-size: 14px;
                box-sizing: border-box;
                min-height: 120px;
              "
            />
            <div style="text-align: right; color: #909399; font-size: 12px; margin-top: 4px">
              {{ reviewForm.opinion.length }}/5000
            </div>
          </div>

          <div class="review-panel-actions">
            <template v-if="!isPublished">
              <el-button type="success" :icon="Check" size="large" @click="handleApprove">
                批准
              </el-button>
              <el-button type="danger" :icon="Close" size="large" @click="handleReject">
                驳回
              </el-button>
            </template>
            <template v-else>
              <el-button type="warning" :icon="Warning" size="large" @click="handleSendAdvice">
                发送建议
              </el-button>
              <el-button type="danger" :icon="Delete" size="large" @click="handleUnpublish">
                下架
              </el-button>
            </template>
          </div>

          <div
            v-if="page.draftStatus === 'SAVED'"
            style="margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--var(--color-border))"
          >
            <div style="color: var(--var(--color-muted)); font-size: 13px">
              <el-icon><InfoFilled /></el-icon>
              当前页面为保存态，尚未提交审核。点击"批准"将直接发布该页面。
            </div>
          </div>

          <div
            v-if="isPublished"
            style="margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--var(--color-border))"
          >
            <div style="color: var(--var(--color-muted)); font-size: 13px">
              <el-icon><InfoFilled /></el-icon>
              当前页面已发布。点击"发送建议"将向创作者发送修改建议，点击"下架"将取消发布状态并通知创作者。
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="hw-card">
    <el-empty description="页面不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPage, approvePage, rejectPage, unpublishPage, sendAdvice } from '../../api/page'
import type { PageDTO } from '../../api/page'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, Close, InfoFilled, Warning, Delete } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const page = ref<PageDTO | null>(null)
const loading = ref(true)
const reviewForm = ref({
  opinion: '',
})

const isPublished = computed(() => page.value?.status === 'PUBLISHED')

onMounted(async () => {
  const slug = route.params.slug as string
  try {
    const res = await getPage(slug)
    if (res.code === 200 && res.data) {
      page.value = res.data
    }
  } catch (error) {
    console.error('获取页面失败:', error)
  } finally {
    loading.value = false
  }
})

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'PUBLISHED':
      return 'success'
    case 'DRAFT':
      return 'warning'
    case 'SAVED':
      return 'info'
    case 'DELETED':
      return 'danger'
    default:
      return ''
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'PUBLISHED':
      return '已发布'
    case 'DRAFT':
      return '待审核'
    case 'SAVED':
      return '保存态'
    case 'DELETED':
      return '已删除'
    default:
      return status
  }
}

const handleApprove = async () => {
  if (!page.value) return

  try {
    const res = await approvePage(page.value.slug, reviewForm.value.opinion)
    if (res.code === 200) {
      ElMessage.success('审核通过')
      router.push('/admin/pages')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

const handleReject = async () => {
  if (!page.value) return

  try {
    const res = await rejectPage(page.value.slug, reviewForm.value.opinion || '审核未通过')
    if (res.code === 200) {
      ElMessage.success('已驳回')
      router.push('/admin/pages')
    }
  } catch (error) {
    console.error('驳回失败:', error)
    ElMessage.error('驳回失败')
  }
}

const handleUnpublish = async () => {
  if (!page.value) return

  try {
    const res = await unpublishPage(page.value.slug, reviewForm.value.opinion || '页面已下架')
    if (res.code === 200) {
      ElMessage.success('已下架')
      router.push('/admin/pages')
    }
  } catch (error) {
    console.error('下架失败:', error)
    ElMessage.error('下架失败')
  }
}

const handleSendAdvice = async () => {
  if (!page.value) return

  if (!reviewForm.value.opinion.trim()) {
    ElMessage.warning('请输入修改建议')
    return
  }

  try {
    const res = await sendAdvice(page.value.slug, reviewForm.value.opinion)
    if (res.code === 200) {
      ElMessage.success('建议已发送')
      reviewForm.value.opinion = ''
    }
  } catch (error) {
    console.error('发送建议失败:', error)
    ElMessage.error('发送建议失败')
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

<style scoped>
.review-page {
  max-width: 1600px;
  margin: 0 auto;
}

.review-page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.review-page-title {
  font-size: 24px;
  color: var(--var(--color-foreground));
  margin: 0;
}

.review-content {
  display: flex;
  gap: 24px;
}

.review-content-left {
  flex: 3;
  min-width: 0;
}

.review-content-right {
  flex: 1;
  max-width: 320px;
  flex-shrink: 0;
  position: sticky;
  top: 80px;
  height: fit-content;
}

.document-scroll {
  max-height: 70vh;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 8px;
}

.document-scroll::-webkit-scrollbar {
  width: 6px;
}

.document-scroll::-webkit-scrollbar-track {
  background: var(--color-mix(in srgb, var(--color-foreground) 4%, transparent));
  border-radius: 3px;
}

.document-scroll::-webkit-scrollbar-thumb {
  background: var(--var(--color-foreground));
  border-radius: 3px;
}

.document-scroll::-webkit-scrollbar-thumb:hover {
  background: var(--classic-accent-secondary);
}

.document-content {
  white-space: pre-wrap;
  word-break: break-word;
  word-wrap: break-word;
  line-height: 1.6;
  font-size: 15px;
  color: var(--var(--color-foreground));
}

.review-panel {
  padding: 20px;
}

.review-panel-title {
  font-size: 16px;
  color: var(--var(--color-foreground));
  border-bottom: 2px solid var(--var(--color-foreground));
  padding-bottom: 8px;
  margin-bottom: 16px;
}

.review-panel-actions {
  display: flex;
  gap: 12px;
}

.review-panel-actions .el-button {
  flex: 1;
}
</style>
