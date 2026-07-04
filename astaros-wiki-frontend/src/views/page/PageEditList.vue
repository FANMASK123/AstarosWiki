<template>
  <div class="page-edit-list">
    <h2>选择要编辑的页面</h2>

    <div v-if="loading" class="loading">
      <el-spinner />
    </div>

    <div v-else-if="pages.length === 0" class="empty">
      <el-icon size="48" color="#999"><Document /></el-icon>
      <p>暂无页面，点击上方"页面创建"创建新页面</p>
    </div>

    <el-table v-else :data="pages" border class="page-table">
      <el-table-column prop="title" label="标题" min-width="200">
        <template #default="scope">
          <router-link :to="`/page/${scope.row.slug}`" class="page-title-link">
            {{ scope.row.title }}
          </router-link>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="120" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row)" size="small">
            {{ getStatusText(scope.row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" width="180">
        <template #default="scope">
          {{ formatDate(getLatestUpdateTime(scope.row)) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="scope">
          <router-link :to="`/pages/edit/${scope.row.slug}`" class="btn-edit">
            <el-icon><Edit /></el-icon>
            编辑
          </router-link>

          <el-dropdown @command="(version: number) => handleRollback(scope.row.slug, version)">
            <span class="el-dropdown-link">
              <el-icon><Clock /></el-icon>
              历史版本
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="version in getVersions(scope.row)"
                  :key="version.versionNumber"
                  :command="version.versionNumber"
                >
                  版本 {{ version.versionNumber }} - {{ formatDate(version.createdAt) }}
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="!versionsMap[scope.row.slug] || versionsMap[scope.row.slug].length === 0"
                  disabled
                >
                  暂无历史版本
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="!loading && pages.length > 0" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { getMyPages, getPageHistory, rollbackPage } from '../../api/page'
import { Document, Edit, Clock, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { PageDTO, PageVersion } from '../../api/page'

const loading = ref(false)
const pages = ref<PageDTO[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const versionsMap = reactive<Record<string, PageVersion[]>>({})

const loadPages = async () => {
  loading.value = true
  try {
    const res = await getMyPages(currentPage.value - 1, pageSize.value)
    if (res.code === 200 && res.data) {
      pages.value = res.data.content || []
      total.value = res.data.total || 0

      for (const page of pages.value) {
        await loadPageVersions(page.slug)
      }
    }
  } catch (error) {
    console.error('加载页面失败:', error)
  } finally {
    loading.value = false
  }
}

const loadPageVersions = async (slug: string) => {
  try {
    const res = await getPageHistory(slug)
    if (res.code === 200 && res.data) {
      versionsMap[slug] = res.data.sort(
        (a: PageVersion, b: PageVersion) => b.versionNumber - a.versionNumber,
      )
    }
  } catch (error) {
    console.error(`加载页面 ${slug} 历史版本失败:`, error)
  }
}

const getVersions = (page: PageDTO) => {
  return versionsMap[page.slug] || []
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadPages()
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
      return '已发布-待审核'
    } else if (page.draftStatus === 'SAVED') {
      return '已发布-保存中'
    }
    return '已发布'
  }
  switch (page.status) {
    case 'DRAFT':
      return '待审核'
    case 'SAVED':
      return '保存中'
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

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const handleRollback = async (slug: string, versionNumber: number) => {
  try {
    const res = await rollbackPage(slug, versionNumber)
    if (res.code === 200) {
      ElMessage.success(`已回滚到版本 ${versionNumber}`)
      await loadPageVersions(slug)
      await loadPages()
    }
  } catch (error) {
    ElMessage.error('回滚失败')
    console.error('回滚失败:', error)
  }
}

onMounted(() => {
  loadPages()
})
</script>

<style scoped>
.page-edit-list {
  max-width: 1200px;
  margin: 0 auto;
}

.page-edit-list h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--var(--color-foreground));
  margin: 0 0 24px 0;
}

.loading {
  display: flex;
  justify-content: center;
  padding: 40px;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  color: var(--var(--color-muted));
}

.empty p {
  margin-top: 16px;
}

.page-table {
  width: 100%;
}

.page-title-link {
  color: var(--var(--color-foreground));
  text-decoration: none;
}

.page-title-link:hover {
  text-decoration: underline;
}

.btn-edit {
  color: var(--var(--color-foreground));
  font-size: 12px;
  padding: 4px 8px;
  margin-right: 8px;
}

.el-dropdown-link {
  cursor: pointer;
  color: var(--var(--color-foreground));
  font-size: 12px;
  padding: 4px 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
