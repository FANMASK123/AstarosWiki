<template>
  <div>
    <div v-if="loading" class="loading"><div class="spinner"></div></div>
    <template v-else-if="page">
      <article class="card">
        <p class="eyebrow">页面</p>
        <h1 class="page-title">{{ page.title }}</h1>
        <div class="row mb-16"><span class="tag" v-if="page.categoryName">{{ page.categoryName }}</span><span class="tag" v-for="t in page.tags" :key="t">{{ t }}</span></div>
        <div class="page-meta"><span>作者：{{ page.authorName }}</span><span>版本：{{ page.version }}</span><span>浏览：{{ page.viewCount }}</span><span>更新：{{ fmt(page.updatedAt) }}</span></div>
        <div class="divider"></div>
        <div class="page-body" v-html="page.content"></div>
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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { getPageBySlug, deletePage } from '../../api/page'
import type { PageDTO } from '../../api/page'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute(); const router = useRouter(); const userStore = useUserStore()
const page = ref<PageDTO|null>(null); const loading = ref(true)
const canEdit = computed(() => userStore.isLoggedIn() && (userStore.userInfo?.userId === page.value?.authorId || userStore.isAdmin()))

onMounted(async () => {
  try { const r = await getPageBySlug(route.params.slug as string); if (r.code===200) page.value = r.data } catch(e) { console.error(e) } finally { loading.value = false }
})
async function del() {
  if (!page.value) return
  try { await ElMessageBox.confirm('确定删除？','确认',{type:'warning'}); const r = await deletePage(page.value.slug); if (r.code===200) { ElMessage.success('已删除'); router.push('/') } } catch(e) { console.error(e) }
}
function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN',{year:'numeric',month:'long',day:'numeric',hour:'2-digit',minute:'2-digit'}) }
</script>
