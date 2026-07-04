<template>
  <div>
    <div v-if="categoryStore.loading" class="loading"><div class="spinner"></div></div>
    <div v-else-if="categories.length===0" style="padding:20px 14px"><span class="text-muted">暂无分类</span></div>
    <ul v-else class="cat-root">
      <li v-for="c in categories" :key="c.id" class="cat-node">
        <div class="cat-row" @click="nav(`/category/${c.id}`)">
          <button v-if="c.children?.length" class="cat-toggle" :class="{open:open1.has(c.id)}" @click.stop="t1(c.id)">▸</button>
          <span v-else class="cat-toggle" style="visibility:hidden"></span>
          <span class="cat-link"><span class="cat-label">{{ c.name }}</span><span v-if="c.children?.length" class="cat-count">{{ c.children.length }}</span></span>
        </div>
        <Transition name="cat-expand">
          <ul v-if="c.children?.length && open1.has(c.id)" class="cat-children">
            <li v-for="ch in c.children" :key="ch.id" class="cat-node">
              <div class="cat-row" @click="nav(`/category/${ch.id}`)">
                <button v-if="ch.children?.length" class="cat-toggle" :class="{open:open2.has(ch.id)}" @click.stop="t2(ch.id)">▸</button>
                <span v-else class="cat-toggle" style="visibility:hidden"></span>
                <span class="cat-link"><span class="cat-label">{{ ch.name }}</span><span v-if="ch.children?.length" class="cat-count">{{ ch.children.length }}</span></span>
              </div>
              <Transition name="cat-expand">
                <ul v-if="ch.children?.length && open2.has(ch.id)" class="cat-children">
                  <li v-for="gc in ch.children" :key="gc.id" class="cat-node">
                    <div class="cat-row" @click="nav(`/category/${gc.id}`)">
                      <span class="cat-toggle" style="visibility:hidden"></span>
                      <span class="cat-link"><span class="cat-label">{{ gc.name }}</span></span>
                    </div>
                  </li>
                </ul>
              </Transition>
            </li>
          </ul>
        </Transition>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCategoryStore } from '../stores/category'

const router = useRouter()
const categoryStore = useCategoryStore()
const open1 = ref(new Set<number>())
const open2 = ref(new Set<number>())
const categories = computed(() => categoryStore.categories)

onMounted(async () => { await categoryStore.fetchCategories() })
function t1(id: number) { if (open1.value.has(id)) open1.value.delete(id); else open1.value.add(id); open1.value = new Set(open1.value) }
function t2(id: number) { if (open2.value.has(id)) open2.value.delete(id); else open2.value.add(id); open2.value = new Set(open2.value) }
function nav(path: string) { open1.value = new Set(); open2.value = new Set(); router.push(path) }
</script>
