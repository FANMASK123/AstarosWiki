<template>
  <div class="page-detail">
    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <div class="loading-pulse"></div>
    </div>

    <!-- Page content -->
    <div v-else-if="page" class="detail-content">
      <article class="hermes-card detail-card">
        <!-- Meta label -->
        <p class="hw-eyebrow">页面</p>

        <!-- Title -->
        <h1 class="detail-title">{{ page.title }}</h1>

        <!-- Tags -->
        <div class="detail-tags">
          <span class="hw-tag" v-if="page.categoryName">{{ page.categoryName }}</span>
          <span class="hw-tag" v-for="tag in page.tags" :key="tag">{{ tag }}</span>
        </div>

        <!-- Meta -->
        <div class="detail-meta">
          <span>作者：{{ page.authorName }}</span>
          <span>版本：{{ page.version }}</span>
          <span>浏览：{{ page.viewCount }} 次</span>
          <span>更新：{{ formatDate(page.updatedAt) }}</span>
        </div>

        <!-- Divider -->
        <hr class="hw-divider" />

        <!-- Content -->
        <div class="detail-body" v-html="page.content"></div>

        <!-- Actions -->
        <div v-if="canEdit" class="detail-actions">
          <router-link :to="`/page/${page.slug}/edit`" class="hw-btn-ghost">
            编辑 <span>→</span>
          </router-link>
          <router-link :to="`/page/${page.slug}/history`" class="hw-btn-ghost">
            历史版本
          </router-link>
          <button v-if="userStore.isAdmin()" class="hw-btn-ghost" @click="handleDelete">
            删除
          </button>
        </div>
      </article>

      <!-- Related pages -->
      <section class="related-section">
        <p class="hw-eyebrow">相关</p>
        <h2 class="hw-section-title">相关页面</h2>
        <div v-if="relatedPages.length === 0" class="text-muted" style="padding: 20px 0">
          暂无相关页面
        </div>
        <div v-else class="related-grid">
          <router-link
            v-for="related in relatedPages"
            :key="related.id"
            :to="`/page/${related.slug}`"
            class="hermes-card related-card"
          >
            <h3 class="related-title">{{ related.title }}</h3>
            <span class="hw-tag" v-if="related.categoryName">{{ related.categoryName }}</span>
          </router-link>
        </div>
      </section>
    </div>

    <!-- Empty -->
    <div v-else class="empty-state">
      <p class="hw-eyebrow">404</p>
      <h2 class="hw-section-title">页面不存在</h2>
      <p class="text-muted">您查找的页面不存在或已被删除。</p>
      <router-link to="/" class="hw-btn-ghost" style="margin-top: 20px; display: inline-flex">
        返回首页
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { getPageBySlug, deletePage } from '../../api/page'
import type { PageDTO } from '../../api/page'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const page = ref<PageDTO | null>(null)
const loading = ref(true)
const relatedPages = ref<PageDTO[]>([])

const canEdit = computed(
  () =>
    userStore.isLoggedIn() &&
    (userStore.userInfo?.userId === page.value?.authorId || userStore.isAdmin()),
)

onMounted(async () => {
  const slug = route.params.slug as string
  try {
    const res = await getPageBySlug(slug)
    if (res.code === 200) {
      page.value = res.data
    }
  } catch (error) {
    console.error('获取页面失败:', error)
  } finally {
    loading.value = false
  }
})

async function handleDelete() {
  if (!page.value) return
  try {
    await ElMessageBox.confirm('确定要删除这个页面吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const res = await deletePage(page.value.slug)
    if (res.code === 200) {
      ElMessage.success('页面已删除')
      router.push('/')
    }
  } catch {
    // cancelled
  }
}

function formatDate(date: string) {
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
.page-detail {
  max-width: var(--max-width-container);
  margin: 0 auto;
  padding: 32px 24px;
}

.detail-card {
  padding: 40px;
}

.detail-title {
  font-family: var(--font-expanded);
  font-size: clamp(1.8rem, 4vw, 2.8rem);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  line-height: 1.1;
  margin-top: 12px;
  color: var(--color-foreground);
}

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 16px;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 12px;
  font-family: var(--font-mono);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--color-muted-foreground);
}

.detail-body {
  font-size: 16px;
  line-height: 1.8;
  color: var(--color-foreground);
}
.detail-body :deep(p) {
  margin-bottom: 1em;
}
.detail-body :deep(h1),
.detail-body :deep(h2),
.detail-body :deep(h3) {
  font-family: var(--font-expanded);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-top: 2em;
  margin-bottom: 0.75em;
}
.detail-body :deep(h2) {
  font-size: 1.3rem;
}
.detail-body :deep(h3) {
  font-size: 1.1rem;
}
.detail-body :deep(a) {
  color: var(--color-foreground);
  text-decoration: underline;
  text-underline-offset: 3px;
  text-decoration-color: var(--color-muted-foreground);
}
.detail-body :deep(a):hover {
  text-decoration-color: var(--color-foreground);
}
.detail-body :deep(blockquote) {
  border-left: 2px solid var(--color-foreground);
  padding-left: 16px;
  margin: 1.5em 0;
  color: var(--color-muted-foreground);
  font-style: italic;
}

.detail-actions {
  display: flex;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid var(--color-border);
}

/* Related */
.related-section {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid var(--color-border);
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 8px;
  margin-top: 16px;
}

.related-card {
  display: block;
}

.related-title {
  font-family: var(--font-expanded);
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: var(--color-foreground);
  margin-bottom: 8px;
}

/* Loading */
.loading-state {
  display: flex;
  justify-content: center;
  padding: 80px 0;
}
.loading-pulse {
  width: 32px;
  height: 32px;
  border: 2px solid var(--color-border);
  border-top-color: var(--color-foreground);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Empty */
.empty-state {
  text-align: center;
  padding: 80px 24px;
}

@media (max-width: 640px) {
  .page-detail {
    padding: 16px 12px;
  }
  .detail-card {
    padding: 20px;
  }
  .detail-actions {
    flex-direction: column;
  }
}
</style>
