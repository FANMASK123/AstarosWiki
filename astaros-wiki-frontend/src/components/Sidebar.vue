<template>
  <div style="height:100%;display:flex;flex-direction:column">
    <div style="padding:12px 16px;border-bottom:1px solid var(--border)">
      <span class="eyebrow">功能菜单</span>
    </div>
    <div style="flex:1;overflow-y:auto">
      <button class="cat-row" style="width:100%;padding:12px 16px;background:none;border:none;cursor:pointer;font:inherit;color:var(--fg);font-size:13px;text-align:left" @click="open = !open">
        <span style="flex:1">📁 分类导航</span>
        <span style="transition:transform .2s;transform:rotate({{ open ? '90deg' : '0deg' }});font-size:10px;color:var(--muted)">▸</span>
      </button>
      <CategoryNav />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CategoryNav from './CategoryNav.vue'
const open = ref(true)
</script>
