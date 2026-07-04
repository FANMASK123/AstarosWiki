<template>
  <div>
    <div class="hw-card">
      <div class="hw-eyebrow">
        {{ isEdit ? '编辑页面' : '创建页面' }}
      </div>
      <div class="hw-card-content">
        <el-form :model="pageForm" label-width="100px">
          <el-form-item label="标题">
            <el-input v-model="pageForm.title" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="pageForm.categoryId" placeholder="请选择分类" style="width: 100%">
              <el-option
                v-for="category in categoryOptions"
                :key="category.id"
                :label="category.name"
                :value="category.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="标签">
            <el-select
              v-model="pageForm.tagNames"
              multiple
              filterable
              allow-create
              placeholder="请选择标签"
              style="width: 100%"
            >
              <el-option
                v-for="tag in tagStore.tags"
                :key="tag.id"
                :label="tag.name"
                :value="tag.name"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="内容">
            <el-input
              v-model="pageForm.content"
              type="textarea"
              :rows="20"
              placeholder="请输入页面内容"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleSubmit(false)"
              >保存</el-button
            >
            <el-button type="success" :loading="loading" @click="handleSubmit(true)"
              >保存并提交审核</el-button
            >
            <el-button v-if="isEdit" type="info" @click="showHistoryDrawer = true"
              >历史版本</el-button
            >
            <el-button @click="$router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 历史版本抽屉 -->
    <el-drawer v-model="showHistoryDrawer" title="历史版本" direction="rtl" size="50%">
      <div v-if="historyLoading">加载中...</div>
      <div v-else-if="historyVersions.length === 0">
        <el-empty description="暂无历史版本" />
      </div>
      <div v-else>
        <div class="history-list">
          <div
            v-for="version in historyVersions"
            :key="version.id"
            class="history-item"
            :class="{ current: version.versionNumber === currentPageVersion }"
          >
            <div class="version-header">
              <span class="version-number">版本 {{ version.versionNumber }}</span>
              <span v-if="version.versionNumber === currentPageVersion" class="current-badge"
                >当前版本</span
              >
              <span class="version-date">{{ formatDate(version.createdAt) }}</span>
            </div>
            <div class="version-actions">
              <el-button size="small" @click="previewVersion(version)">查看内容</el-button>
              <el-button
                v-if="version.versionNumber !== currentPageVersion"
                size="small"
                type="warning"
                @click="handleRollback(version.versionNumber)"
                >回滚到此版本</el-button
              >
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 版本内容预览对话框 -->
    <el-dialog v-model="showPreviewDialog" title="版本内容预览" width="60%">
      <div class="preview-content">
        <pre>{{ previewContent }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { createPage, updatePage, getPage, getPageHistory, rollbackPage } from '../../api/page'
import type { PageVersion, PageDTO } from '../../api/page'
import { useCategoryStore } from '../../stores/category'
import { useTagStore } from '../../stores/tag'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const categoryStore = useCategoryStore()
const tagStore = useTagStore()

const isEdit = ref(false)
const loading = ref(false)
const currentPageVersion = ref(1)

const pageForm = reactive({
  title: '',
  categoryId: undefined as number | undefined,
  tagNames: [] as string[],
  content: '',
})

const categoryOptions = ref<any[]>([])

// 历史版本相关
const showHistoryDrawer = ref(false)
const historyLoading = ref(false)
const historyVersions = ref<PageVersion[]>([])
const showPreviewDialog = ref(false)
const previewContent = ref('')

onMounted(async () => {
  await categoryStore.fetchCategories()
  await tagStore.fetchTags()

  const flatCategories = (categories: any[]): any[] => {
    let result: any[] = []
    for (const cat of categories) {
      result.push(cat)
      if (cat.children && cat.children.length > 0) {
        result = result.concat(flatCategories(cat.children))
      }
    }
    return result
  }
  categoryOptions.value = flatCategories(categoryStore.categories)

  const slug = route.params.slug as string
  if (slug) {
    isEdit.value = true
    const res = await getPage(slug)
    if (res.code === 200 && res.data) {
      const page = res.data as PageDTO
      pageForm.title = page.title
      pageForm.categoryId = page.categoryId || undefined
      pageForm.tagNames = page.tags ? [...page.tags] : []
      // 优先使用草稿内容，如果没有草稿则使用已发布内容
      pageForm.content = page.draftContent || page.content || ''
      currentPageVersion.value = page.version
    }
  }
})

// 加载历史版本
const loadHistory = async () => {
  if (!isEdit.value) return

  historyLoading.value = true
  try {
    const slug = route.params.slug as string
    const res = await getPageHistory(slug)
    if (res.code === 200) {
      historyVersions.value = res.data || []
    }
  } catch (error) {
    console.error('获取历史版本失败:', error)
  } finally {
    historyLoading.value = false
  }
}

// 监听抽屉打开时加载历史
watch(showHistoryDrawer, (newVal) => {
  if (newVal && isEdit.value) {
    loadHistory()
  }
})

const generateSlug = (title: string): string => {
  return title
    .toLowerCase()
    .replace(/[^a-zA-Z0-9\u4e00-\u9fa5]+/g, '-')
    .replace(/^-|-$/g, '')
    .substring(0, 100)
}

const handleSubmit = async (publish: boolean) => {
  loading.value = true
  try {
    const data = {
      title: pageForm.title,
      content: pageForm.content,
      slug: generateSlug(pageForm.title),
      categoryId: pageForm.categoryId,
      tags: pageForm.tagNames,
      publish,
    }

    let res
    if (isEdit.value) {
      res = await updatePage(route.params.slug as string, data)
    } else {
      res = await createPage(data)
    }

    if (res.code === 200) {
      if (publish) {
        ElMessage.success(isEdit.value ? '已提交审核' : '已提交审核')
      } else {
        ElMessage.success(isEdit.value ? '保存成功' : '创建成功')
      }
      router.push('/pages/manage')
    }
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    loading.value = false
  }
}

// 预览版本内容
const previewVersion = (version: PageVersion) => {
  previewContent.value = version.content || '(无内容)'
  showPreviewDialog.value = true
}

// 回滚版本
const handleRollback = async (versionNumber: number) => {
  try {
    await ElMessageBox.confirm(
      `确定要回滚到版本 ${versionNumber} 吗？当前内容将被替换。`,
      '版本回滚确认',
      {
        confirmButtonText: '确定回滚',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    const slug = route.params.slug as string
    const res = await rollbackPage(slug, versionNumber)
    if (res.code === 200) {
      ElMessage.success('回滚成功')
      // 更新当前内容
      if (res.data) {
        pageForm.content = res.data.content || ''
        currentPageVersion.value = res.data.version
      }
      // 重新加载历史
      loadHistory()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('回滚失败:', error)
    }
  }
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleString('zh-CN')
}
</script>

<style scoped>
.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  padding: 16px;
  border: 1px solid var(--var(--color-border));
  border-radius: 8px;
  background-color: var(--var(--color-card));
}

.history-item.current {
  border-color: var(--var(--color-foreground));
  background-color: rgba(var(--var(--color-foreground)-rgb), 0.1);
}

.version-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.version-number {
  font-weight: bold;
  font-size: 16px;
}

.current-badge {
  padding: 2px 8px;
  background-color: var(--var(--color-foreground));
  color: white;
  border-radius: 4px;
  font-size: 12px;
}

.version-date {
  color: var(--var(--color-muted));
  font-size: 14px;
}

.version-actions {
  display: flex;
  gap: 8px;
}

.preview-content {
  max-height: 60vh;
  overflow-y: auto;
  padding: 16px;
  background-color: var(--color-mix(in srgb, var(--color-foreground) 4%, transparent));
  border-radius: 8px;
}

.preview-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: monospace;
  font-size: 14px;
  line-height: 1.6;
}
</style>
