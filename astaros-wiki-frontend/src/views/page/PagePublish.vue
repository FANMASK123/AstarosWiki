<template>
  <div class="page-publish">
    <div class="page-publish-header">
      <h2>页面发布管理</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="保存中" name="saved">
        <div class="page-publish-list">
          <div v-if="loading" class="loading">
            <el-spinner />
          </div>
          <div v-else-if="pages.length === 0" class="empty">
            <el-icon size="48" color="#999"><Document /></el-icon>
            <p>暂无保存态页面</p>
          </div>
          <el-table v-else :data="pages" border class="page-table">
            <el-table-column prop="title" label="标题" min-width="200">
              <template #default="scope">
                <router-link :to="`/pages/edit/${scope.row.slug}`" class="page-title-link">
                  {{ scope.row.title }}
                </router-link>
              </template>
            </el-table-column>
            <el-table-column prop="categoryName" label="分类" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="small">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="更新时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.updatedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <router-link :to="`/pages/edit/${scope.row.slug}`" class="btn-edit">
                  <el-icon><Edit /></el-icon>
                  编辑
                </router-link>
                <el-button type="primary" size="small" @click="handleSubmit(scope.row.slug)">
                  提交审核
                </el-button>
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
      </el-tab-pane>

      <el-tab-pane label="待审核" name="draft">
        <div class="page-publish-list">
          <div v-if="loading" class="loading">
            <el-spinner />
          </div>
          <div v-else-if="pages.length === 0" class="empty">
            <el-icon size="48" color="#999"><Document /></el-icon>
            <p>暂无待审核页面</p>
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
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="small">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="rejectReason" label="驳回原因" width="200">
              <template #default="scope">
                <span v-if="scope.row.rejectReason" style="color: #f56c6c">{{
                  scope.row.rejectReason
                }}</span>
                <span v-else style="color: #909399">暂无</span>
              </template>
            </el-table-column>
            <el-table-column prop="updatedAt" label="提交时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.updatedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <router-link :to="`/pages/edit/${scope.row.slug}`" class="btn-edit">
                  <el-icon><Edit /></el-icon>
                  编辑
                </router-link>
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
      </el-tab-pane>

      <el-tab-pane label="已发布" name="published">
        <div class="page-publish-list">
          <div v-if="loading" class="loading">
            <el-spinner />
          </div>
          <div v-else-if="pages.length === 0" class="empty">
            <el-icon size="48" color="#999"><Document /></el-icon>
            <p>暂无已发布页面</p>
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
            <el-table-column prop="viewCount" label="浏览" width="100" />
            <el-table-column prop="updatedAt" label="更新时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.updatedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <router-link :to="`/pages/edit/${scope.row.slug}`" class="btn-edit">
                  <el-icon><Edit /></el-icon>
                  编辑
                </router-link>
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
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyPagesByStatus, updatePage } from '../../api/page'
import { Document, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('saved')
const loading = ref(false)
const pages = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadPages = async () => {
  loading.value = true
  try {
    const statusMap: Record<string, string> = {
      saved: 'SAVED',
      draft: 'DRAFT',
      published: 'PUBLISHED',
    }
    const status = statusMap[activeTab.value] || 'SAVED'
    const res = await getMyPagesByStatus(status, currentPage.value - 1, pageSize.value)
    if (res.code === 200 && res.data) {
      pages.value = res.data.content || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载页面失败:', error)
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  currentPage.value = 1
  loadPages()
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  loadPages()
}

const handleSubmit = async (slug: string) => {
  try {
    const res = await updatePage(slug, { publish: true })
    if (res.code === 200) {
      ElMessage.success('已提交审核，请等待管理员审核')
      loadPages()
    }
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'PUBLISHED':
      return 'success'
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

const getStatusText = (status: string) => {
  switch (status) {
    case 'PUBLISHED':
      return '已发布'
    case 'DRAFT':
      return '待审核'
    case 'SAVED':
      return '保存中'
    case 'REJECTED':
      return '未通过'
    default:
      return status
  }
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

onMounted(() => {
  loadPages()
})
</script>

<style scoped>
.page-publish {
  max-width: 1200px;
  margin: 0 auto;
}

.page-publish-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-publish-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--fg);
  margin: 0;
}

.toggle-switch {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: var(--muted);
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
  color: var(--muted);
}

.empty p {
  margin-top: 16px;
}

.page-table {
  width: 100%;
}

.page-title-link {
  color: var(--fg);
  text-decoration: none;
}

.page-title-link:hover {
  text-decoration: underline;
}

.btn-edit {
  color: var(--fg);
  font-size: 12px;
  padding: 4px 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
