<template>
  <div>
    <div class="card">
      <p class="eyebrow">{{ isEdit ? '编辑页面' : '创建页面' }}</p>
      <el-form :model="pageForm" label-width="100px" class="mt-16">
        <el-form-item label="标题"><el-input v-model="pageForm.title" /></el-form-item>
        <el-form-item label="分类">
          <!-- 三级级联选择器 -->
          <div class="cascade-picker">
            <div class="cascade-display" @click="showCascade = !showCascade">
              <span v-if="selectedPath.length">{{ selectedPath.join(' → ') }}</span>
              <span v-else class="text-muted">选择分类（可选三级）</span>
              <span class="cascade-arrow" :class="{open:showCascade}">▾</span>
            </div>
            <Transition name="drop">
              <div v-if="showCascade" class="cascade-drop">
                <div class="cascade-col">
                  <div v-for="c in categoryOptions" :key="c.id"
                    class="cascade-opt" :class="{sel: selL1===c.id}"
                    @click="pickL1(c)">{{ c.name }}
                    <span v-if="c.children?.length" class="cascade-next">▸</span>
                  </div>
                </div>
                <div v-if="selL1Children.length" class="cascade-col">
                  <div v-for="c in selL1Children" :key="c.id"
                    class="cascade-opt" :class="{sel: selL2===c.id}"
                    @click="pickL2(c)">{{ c.name }}
                    <span v-if="c.children?.length" class="cascade-next">▸</span>
                  </div>
                </div>
                <div v-if="selL2Children.length" class="cascade-col">
                  <div v-for="c in selL2Children" :key="c.id"
                    class="cascade-opt" :class="{sel: selL3===c.id}"
                    @click="pickL3(c)">{{ c.name }}</div>
                </div>
              </div>
            </Transition>
          </div>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="pageForm.tagNames" multiple filterable allow-create placeholder="选择标签" style="width:100%">
            <el-option v-for="t in tagStore.tags" :key="t.id" :label="t.name" :value="t.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <MdEditor
            v-model="pageForm.content"
            :style="{ height: '500px', width: '100%' }"
            :toolbars="toolbars"
            @on-upload-img="handleUploadImg"
            placeholder="支持 Markdown 语法..."
          />
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
        <div v-for="v in historyVersions" :key="v.id" class="card" :style="{ borderColor: v.versionNumber===currentPageVersion ? 'var(--fg)' : '' }">
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
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import type { ToolbarNames } from 'md-editor-v3'
import { uploadFile } from '../../api/file'

const route = useRoute()
const categoryStore = useCategoryStore()
const tagStore = useTagStore()
const slug = computed(() => route.params.slug as string || '')
const isEdit = computed(() => !!slug.value)
const loading = ref(false)

const pageForm = reactive({ title:'', content:'', categoryId:null as number|null, tagNames:[] as string[] })
const categoryOptions = ref<CategoryDTO[]>([])

// MdEditor toolbars
const toolbars: ToolbarNames[] = [
  'bold', 'underline', 'italic', 'strikeThrough',
  '-',
  'title', 'quote', 'unorderedList', 'orderedList', 'task',
  '-',
  'code', 'codeRow', 'link', 'image', 'table',
  '-',
  'revoke', 'next',
  '=',
  'preview', 'previewOnly',
]

// Image upload — calls backend FileController
async function handleUploadImg(files: File[], callback: (urls: string[]) => void) {
  const urls: string[] = []
  for (const file of files) {
    try {
      const res = await uploadFile(file)
      if (res.code === 200 && res.data) {
        urls.push(res.data)
      } else {
        ElMessage.error(res.msg || '图片上传失败')
      }
    } catch (e) {
      console.error('Upload failed:', e)
      ElMessage.error('图片上传失败')
    }
  }
  callback(urls)
}

// ── Cascade picker state ──
const showCascade = ref(false)
const selL1 = ref<number|null>(null); const selL2 = ref<number|null>(null); const selL3 = ref<number|null>(null)
const selL1Children = computed(() => categoryOptions.value.find(c=>c.id===selL1.value)?.children??[])
const selL2Children = computed(() => selL1Children.value.find(c=>c.id===selL2.value)?.children??[])
const selectedPath = computed(() => {
  const p: string[] = []
  const l1 = categoryOptions.value.find(c=>c.id===selL1.value); if (l1) p.push(l1.name)
  const l2 = selL1Children.value.find(c=>c.id===selL2.value); if (l2) p.push(l2.name)
  const l3 = selL2Children.value.find(c=>c.id===selL3.value); if (l3) p.push(l3.name)
  return p
})

function pickL1(c: CategoryDTO) { selL1.value=c.id; selL2.value=null; selL3.value=null; if (!c.children?.length) { pageForm.categoryId=c.id; showCascade.value=false } }
function pickL2(c: CategoryDTO) { selL2.value=c.id; selL3.value=null; if (!c.children?.length) { pageForm.categoryId=c.id; showCascade.value=false } }
function pickL3(c: CategoryDTO) { selL3.value=c.id; pageForm.categoryId=c.id; showCascade.value=false }

const showHistory = ref(false); const historyLoading = ref(false)
const historyVersions = ref<PageVersion[]>([]); const currentPageVersion = ref(0)

onMounted(async () => {
  await categoryStore.fetchCategories(); categoryOptions.value=categoryStore.categories
  await tagStore.fetchTags()
  if (isEdit.value) {
    const r = await getPageBySlug(slug.value)
    if (r.code===200) {
      pageForm.title=r.data.title
      pageForm.content=r.data.content
      pageForm.categoryId=r.data.categoryId
      pageForm.tagNames=r.data.tags||[]
      currentPageVersion.value=r.data.version
      // Restore cascade selection
      if (r.data.categoryId) restoreCascade(r.data.categoryId)
    }
  }
})

function restoreCascade(catId: number) {
  function walk(nodes: CategoryDTO[], parents: CategoryDTO[]): boolean {
    for (const n of nodes) {
      if (n.id === catId) {
        if (parents.length >= 1) selL1.value = parents[0].id
        if (parents.length >= 2) selL2.value = parents[1].id
        return true
      }
      if (n.children?.length && walk(n.children, [...parents, n])) return true
    }
    return false
  }
  walk(categoryOptions.value, [])
}

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
