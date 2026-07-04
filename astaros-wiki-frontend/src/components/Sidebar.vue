<template>
  <div style="height:100%;display:flex;flex-direction:column">
    <div style="padding:10px 14px;border-bottom:1px solid var(--border)">
      <span class="eyebrow">功能菜单</span>
    </div>
    <div style="flex:1;overflow-y:auto;padding:8px 0">
      <button class="cat-row" style="width:100%;padding:10px 14px;background:none;border:none;cursor:pointer;font:inherit;color:var(--fg);font-size:13px;text-align:left" @click="drawer=true">
        <span style="flex:1">📁 分类导航</span>
        <span style="font-size:10px;color:var(--muted)">▸</span>
      </button>

      <el-drawer v-model="drawer" direction="ltr" title="分类导航" size="320px">
        <CategoryNav @select="drawer=false" />
      </el-drawer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CategoryNav from './CategoryNav.vue'
const drawer = ref(false)
</script>
