<template>
  <div>
    <div class="eyebrow">页面管理</div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="待审核页面" name="pending">
        <el-table :data="pendingPages" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="slug" label="Slug" />
          <el-table-column prop="authorName" label="作者" />
          <el-table-column prop="rejectReason" label="驳回原因">
            <template #default="{ row }">
              <span v-if="row.rejectReason" style="color: #f56c6c">{{ row.rejectReason }}</span>
              <span v-else style="color: #909399">暂无</span>
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="提交时间">
            <template #default="{ row }">
              {{ formatDate(row.updatedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="350">
            <template #default="{ row }">
              <el-button size="small" :icon="View" @click="handleView(row.slug)">查看</el-button>
              <el-button size="small" type="success" :icon="Check" @click="handleApprove(row.slug)">
                审核通过
              </el-button>
              <el-button size="small" type="danger" :icon="Close" @click="handleReject(row.slug)">
                驳回
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="已发布页面" name="published">
        <el-table :data="pages" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="slug" label="Slug" />
          <el-table-column prop="authorName" label="作者" />
          <el-table-column prop="viewCount" label="浏览量" />
          <el-table-column prop="updatedAt" label="更新时间">
            <template #default="{ row }">
              {{ formatDate(row.updatedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="300">
            <template #default="{ row }">
              <el-button size="small" :icon="View" @click="handleView(row.slug)">查看</el-button>
              <el-button
                size="small"
                type="warning"
                :icon="ArrowDown"
                @click="handleUnpublish(row.slug)"
              >
                下架
              </el-button>
              <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(row.slug)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="保存态页面" name="saved">
        <el-table :data="savedPages" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="slug" label="Slug" />
          <el-table-column prop="authorName" label="作者" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row)" size="small">
                {{ getStatusText(row) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="更新时间" width="180">
            <template #default="{ row }">
              {{ formatDate(getLatestUpdateTime(row)) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250">
            <template #default="{ row }">
              <el-button size="small" :icon="View" @click="handleView(row.slug)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="回收站" name="deleted">
        <el-table :data="deletedPages" style="width: 100%">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="slug" label="Slug" />
          <el-table-column prop="updatedAt" label="删除时间">
            <template #default="{ row }">
              {{ formatDate(row.updatedAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" type="primary" :icon="Refresh" @click="handleRestore(row.id)">
                恢复
              </el-button>
              <el-button
                size="small"
                type="danger"
                :icon="Delete"
                @click="handlePermanentDelete(row.id)"
              >
                永久删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="rejectDialogVisible" title="驳回原因" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="原因">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入驳回原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="unpublishDialogVisible" title="下架原因" width="400px">
      <el-form :model="unpublishForm" label-width="80px">
        <el-form-item label="原因">
          <el-input
            v-model="unpublishForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入下架原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="unpublishDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmUnpublish">确认下架</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  getPublishedPages,
  deletePage,
  approvePage,
  rejectPage,
  unpublishPage,
  getSavedPages,
} from '../../api/page'
import { getDeletedPages, restorePage, permanentlyDeletePage } from '../../api/admin'
import { getDraftPages } from '../../api/page'
import type { PageDTO } from '../../api/page'
import { View, Check, Close, Delete, Refresh, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const activeTab = ref('pending')
const pages = ref<PageDTO[]>([])
const pendingPages = ref<PageDTO[]>([])
const deletedPages = ref<PageDTO[]>([])
const savedPages = ref<PageDTO[]>([])

const rejectDialogVisible = ref(false)
const unpublishDialogVisible = ref(false)
const currentSlug = ref('')

const rejectForm = reactive({
  reason: '',
})

const unpublishForm = reactive({
  reason: '',
})

onMounted(async () => {
  await loadPendingPages()
  await loadPages()
  await loadDeletedPages()
  await loadSavedPages()
})

watch(activeTab, async () => {
  if (activeTab.value === 'pending') {
    await loadPendingPages()
  } else if (activeTab.value === 'published') {
    await loadPages()
  } else if (activeTab.value === 'saved') {
    await loadSavedPages()
  } else {
    await loadDeletedPages()
  }
})

const loadPages = async () => {
  try {
    const res = await getPublishedPages(0, 20)
    if (res.code === 200) {
      pages.value = res.data.content
    }
  } catch (error) {
    console.error('加载页面失败:', error)
  }
}

const loadPendingPages = async () => {
  try {
    const res = await getDraftPages(0, 20)
    if (res.code === 200) {
      pendingPages.value = res.data.content
    }
  } catch (error) {
    console.error('加载待审核页面失败:', error)
  }
}

const loadDeletedPages = async () => {
  try {
    const res = await getDeletedPages(0, 20)
    if (res.code === 200) {
      deletedPages.value = res.data.content
    }
  } catch (error) {
    console.error('加载删除页面失败:', error)
  }
}

const handleView = (slug: string) => {
  router.push(`/admin/pages/review/${slug}`)
}

const handleApprove = async (slug: string) => {
  try {
    const res = await approvePage(slug)
    if (res.code === 200) {
      ElMessage.success('审核通过')
      await loadPendingPages()
      await loadPages()
    }
  } catch (error) {
    console.error('审核失败:', error)
  }
}

const handleReject = (slug: string) => {
  currentSlug.value = slug
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  try {
    const res = await rejectPage(currentSlug.value, rejectForm.reason)
    if (res.code === 200) {
      ElMessage.success('已驳回')
      rejectDialogVisible.value = false
      await loadPendingPages()
    }
  } catch (error) {
    console.error('驳回失败:', error)
  }
}

const handleUnpublish = (slug: string) => {
  currentSlug.value = slug
  unpublishForm.reason = ''
  unpublishDialogVisible.value = true
}

const confirmUnpublish = async () => {
  try {
    const res = await unpublishPage(currentSlug.value, unpublishForm.reason)
    if (res.code === 200) {
      ElMessage.success('已下架')
      unpublishDialogVisible.value = false
      await loadPages()
      await loadPendingPages()
    }
  } catch (error) {
    console.error('下架失败:', error)
  }
}

const handleDelete = async (slug: string) => {
  try {
    await ElMessageBox.confirm('确定要删除这个页面吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const res = await deletePage(slug)
    if (res.code === 200) {
      ElMessage.success('已删除')
      await loadPages()
      await loadDeletedPages()
    }
  } catch (error) {
    console.error('删除失败:', error)
  }
}

const handleRestore = async (pageId: number) => {
  try {
    await restorePage(pageId)
    ElMessage.success('已恢复')
    await loadPages()
    await loadDeletedPages()
  } catch (error) {
    console.error('恢复失败:', error)
  }
}

const handlePermanentDelete = async (pageId: number) => {
  try {
    await ElMessageBox.confirm('确定要永久删除这个页面吗？此操作不可恢复。', '确认永久删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await permanentlyDeletePage(pageId)
    ElMessage.success('已永久删除')
    await loadDeletedPages()
  } catch (error) {
    console.error('永久删除失败:', error)
  }
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN')
}

const loadSavedPages = async () => {
  try {
    const res = await getSavedPages(0, 20)
    if (res.code === 200) {
      savedPages.value = res.data.content
    }
  } catch (error) {
    console.error('加载保存态页面失败:', error)
  }
}

const getStatusType = (page: PageDTO) => {
  if (page.status === 'PUBLISHED') {
    if (page.draftStatus === 'DRAFT') {
      return 'warning'
    } else if (page.draftStatus === 'SAVED') {
      return 'info'
    }
    return 'success'
  }
  switch (page.status) {
    case 'DRAFT':
      return 'warning'
    case 'SAVED':
      return 'info'
    case 'REJECTED':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (page: PageDTO) => {
  if (page.status === 'PUBLISHED') {
    if (page.draftStatus === 'DRAFT') {
      return '已发布(待审核)'
    } else if (page.draftStatus === 'SAVED') {
      return '已发布(保存态)'
    }
    return '已发布'
  }
  switch (page.status) {
    case 'DRAFT':
      return '预备审核'
    case 'SAVED':
      return '保存态'
    case 'REJECTED':
      return '未通过'
    default:
      return page.status
  }
}

const getLatestUpdateTime = (page: PageDTO) => {
  if (page.draftUpdatedAt) {
    return page.draftUpdatedAt
  }
  return page.updatedAt
}
</script>
