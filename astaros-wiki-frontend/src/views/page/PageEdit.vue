<template>
  <div>
    <div class="card">
      <p class="eyebrow">{{ isEdit ? '编辑页面' : '创建页面' }}</p>
      <el-form :model="pageForm" label-width="100px" class="mt-16">
        <el-form-item label="标题"><el-input v-model="pageForm.title" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="pageForm.categoryId" placeholder="选择分类" style="width:100%">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="pageForm.tagNames" multiple filterable allow-create placeholder="选择标签" style="width:100%">
            <el-option v-for="t in tagStore.tags" :key="t.id" :label="t.name" :value="t.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="pageForm.content" type="textarea" :rows="16" placeholder="页面内容" style="width:100%" />
        </el-form-item>
        <el-form-item>
          <div class="row">
            <button class="btn btn-ghost" :disabled="loading" @click="handleSubmit(false)">保存</button>
            <button class="btn btn-primary" :disabled="loading" @click="handleSubmit(true)">保存并提交审核 <span>→</span></button>
            <button v-if="isEdit" class="btn btn-ghost" @click="showHistory=true">历史版本</button>
            <button class="btn btn-ghost" @click="$router.back()">取消</button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <el-drawer v-model="showHistory" title="历史版本" direction="rtl" size="50%">
      <div v-if="historyLoading" class="loading"><div class="spinner"></div></div>
      <div v-else-if="historyVersions.length===0" class="empty"><p class="text-muted">暂无历史版本</p></div>
      <div v-else class="stack">
        <div v-for="v in historyVersions" :key="v.id" class="card" :style="{borderColor: v.versionNumber===currentPageVersion ? 'var(--fg)' : ''}">
          <div class="row" style="justify-content:space-between">
            <div><strong>版本 {{ v.versionNumber }}</strong><br><span class="mono">{{ fmt(v.createdAt) }}</span></div>
            <button class="btn btn-ghost" style="font-size:11px;padding:4px 10px" @click="rollbackTo(v.versionNumber)">回滚到此版本</button>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useCategoryStore } from '../../stores/category'
import { useTagStore } from '../../stores/tag'
import { createPage, updatePage, getPageBySlug, getPageHistory, rollbackPage } from '../../api/page'
import type { CategoryDTO } from '../../api/category'
import type { PageVersion } from '../../api/page'
import { ElMessage } from 'element-plus'

const route = useRoute()
const categoryStore = useCategoryStore()
const tagStore = useTagStore()
const slug = computed(() => route.params.slug as string || '')
const isEdit = computed(() => !!slug.value)
const loading = ref(false)

const pageForm = reactive({ title:'', content:'', categoryId:null as number|null, tagNames:[] as string[] })
const categoryOptions = ref<CategoryDTO[]>([])

const showHistory = ref(false); const historyLoading = ref(false)
const historyVersions = ref<PageVersion[]>([]); const currentPageVersion = ref(0)

onMounted(async () => {
  await categoryStore.fetchCategories(); categoryOptions.value=categoryStore.categories
  await tagStore.fetchTags()
  if (isEdit.value) { const r = await getPageBySlug(slug.value); if (r.code===200) { pageForm.title=r.data.title; pageForm.content=r.data.content; pageForm.categoryId=r.data.categoryId; pageForm.tagNames=r.data.tags||[]; currentPageVersion.value=r.data.version } }
})

watch(showHistory, async (v) => { if (v) { historyLoading.value=true; const r = await getPageHistory(slug.value); if (r.code===200) historyVersions.value=r.data; historyLoading.value=false } })

async function handleSubmit(publish: boolean) {
  loading.value=true
  try {
    const data = { title:pageForm.title, content:pageForm.content, categoryId:pageForm.categoryId??undefined, tags:pageForm.tagNames, publish }
    const r = isEdit.value ? await updatePage(slug.value, data) : await createPage(data)
    if (r.code===200) { ElMessage.success(publish?'已提交审核':'已保存'); if (!isEdit.value) window.location.href=`/page/${r.data.slug}` }
  } catch(e) { console.error(e) } finally { loading.value=false }
}

async function rollbackTo(vn: number) {
  const r = await rollbackPage(slug.value, vn)
  if (r.code===200) { ElMessage.success('已回滚'); showHistory.value=false; pageForm.content=r.data.content; currentPageVersion.value=r.data.version }
}

function fmt(d: string) { return new Date(d).toLocaleDateString('zh-CN',{year:'numeric',month:'short',day:'numeric',hour:'2-digit',minute:'2-digit'}) }
</script>
