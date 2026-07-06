<template>
  <div class="pd-root">
    <div v-if="loading" class="loading"><div class="spinner"></div></div>
    <template v-else-if="page">
      <!-- Breadcrumb -->
      <nav class="breadcrumb">
        <router-link to="/">首页</router-link>
        <span class="bc-sep">/</span>
        <router-link v-if="page.categoryId" :to="`/category/${page.categoryId}`">{{ page.categoryName }}</router-link>
        <span class="bc-sep">/</span>
        <span class="bc-current">{{ page.title }}</span>
      </nav>

      <article class="card">
        <p class="eyebrow">页面</p>
        <h1 class="page-title">{{ page.title }}</h1>
        <div class="row mb-16">
          <span class="tag" v-if="page.categoryName">{{ page.categoryName }}</span>
          <span class="tag" v-for="t in page.tags" :key="t">{{ t }}</span>
        </div>
        <div class="page-meta">
          <span>作者：{{ page.authorName }}</span>
          <span>版本：{{ page.version }}</span>
          <span>浏览：{{ page.viewCount }}</span>
          <span>更新：{{ fmt(page.updatedAt) }}</span>
        </div>
        <div class="divider"></div>

        <div class="pd-body-wrap">
          <div class="page-body" ref="bodyRef" v-html="page.content"></div>
          <!-- TOC sidebar -->
          <aside v-if="tocItems.length > 1" class="pd-toc">
            <p class="eyebrow" style="font-size:9px">目录</p>
            <nav class="toc-nav">
              <a v-for="item in tocItems" :key="item.id"
                 :href="'#' + item.id"
                 :class="['toc-link', 'toc-h' + item.level]"
                 @click.prevent="scrollTo(item.id)">{{ item.text }}</a>
            </nav>
          </aside>
        </div>

        <div v-if="canEdit" class="divider"></div>
        <div v-if="canEdit" class="row">
          <router-link :to="`/page/${page.slug}/edit`" class="btn btn-ghost">编辑 →</router-link>
          <router-link :to="`/page/${page.slug}/history`" class="btn btn-ghost">历史</router-link>
          <button v-if="userStore.isAdmin()" class="btn btn-ghost" @click="del">删除</button>
        </div>
      </article>
    </template>
    <div v-else class="empty"><p class="eyebrow">404</p><h2 class="section-title">页面不存在</h2></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { getPageBySlug, deletePage } from '../../api/page'
import type { PageDTO } from '../../api/page'
import { ElMessage, ElMessageBox } from 'element-plus'
import hljs from 'highlight.js'

const route = useRoute(); const router = useRouter(); const userStore = useUserStore()
const page = ref<PageDTO|null>(null); const loading = ref(true)
const bodyRef = ref<HTMLElement|null>(null)
const tocItems = ref<{id:string,text:string,level:number}[]>([])

const canEdit = computed(() => userStore.isLoggedIn() && (userStore.userInfo?.userId === page.value?.authorId || userStore.isAdmin()))

onMounted(async () => {
  try {
    const r = await getPageBySlug(route.params.slug as string)
    if (r.code === 200) page.value = r.data
  } catch(e) { console.error(e) } finally { loading.value = false }
  await nextTick()
  buildToc()
  highlightCode()
})

watch(() => route.params.slug, async () => {
  if (!route.params.slug) return
  loading.value = true; page.value = null
  try {
    const r = await getPageBySlug(route.params.slug as string)
    if (r.code === 200) page.value = r.data
  } catch(e) { console.error(e) } finally { loading.value = false }
  await nextTick()
  buildToc()
  highlightCode()
})

function buildToc() {
  if (!bodyRef.value) return
  const heads = bodyRef.value.querySelectorAll('h1,h2,h3')
  tocItems.value = []
  heads.forEach((h, i) => {
    const level = parseInt(h.tagName[1])
    const text = h.textContent || ''
    const id = h.id || `toc-${i}`
    if (!h.id) h.id = id
    tocItems.value.push({ id, text, level })
  })
}

function highlightCode() {
  if (!bodyRef.value) return
  bodyRef.value.querySelectorAll('pre code').forEach((block) => {
    try { hljs.highlightElement(block as HTMLElement) } catch { /* ignore */ }
  })
}

function scrollTo(id: string) {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function del() {
  if (!page.value) return
  try {
    await ElMessageBox.confirm('确定删除？', '确认', { type: 'warning' })
    const r = await deletePage(page.value.slug)
    if (r.code === 200) { ElMessage.success('已删除'); router.push('/') }
  } catch(e) { console.error(e) }
}

function fmt(d: string) {
  return new Date(d).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.pd-root { max-width: 100%; }

.breadcrumb {
  display: flex; align-items: center; gap: 4px;
  font-size: 12px; color: var(--muted);
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.breadcrumb a { color: var(--muted); transition: color .15s; }
.breadcrumb a:hover { color: var(--fg); }
.bc-sep { opacity: .4; }
.bc-current { color: var(--fg); }

.pd-body-wrap { display: flex; gap: 24px; }
.page-body { flex: 1; min-width: 0; }
.pd-toc {
  flex-shrink: 0; width: 200px;
  position: sticky; top: 20px;
  align-self: flex-start;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}
.toc-nav { display: flex; flex-direction: column; gap: 4px; }
.toc-link {
  font-size: 12px; color: var(--muted);
  transition: color .15s; cursor: pointer;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}
.toc-link:hover { color: var(--fg); }
.toc-h2 { padding-left: 12px; }
.toc-h3 { padding-left: 24px; font-size: 11px; }

@media(max-width: 900px) {
  .pd-body-wrap { flex-direction: column; }
  .pd-toc { display: none; }
}

.page-body :deep(pre) {
  background: color-mix(in srgb, var(--fg), 6%);
  border-radius: 4px; padding: 12px 16px;
  overflow-x: auto; margin: 12px 0;
}
.page-body :deep(code) { font-family: var(--mono); font-size: 13px; }
.page-body :deep(p) { line-height: 1.7; margin: 8px 0; }
.page-body :deep(img) { max-width: 100%; border-radius: 4px; }
.page-body :deep(table) { border-collapse: collapse; width: 100%; margin: 12px 0; }
.page-body :deep(th), .page-body :deep(td) { border: 1px solid var(--border); padding: 8px; }
.page-body :deep(blockquote) { border-left: 3px solid var(--border); padding-left: 16px; margin: 12px 0; color: var(--muted); }
</style>
