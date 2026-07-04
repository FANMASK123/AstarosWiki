<template>
  <div style="height:100%;display:flex;flex-direction:column">
    <div style="padding:10px 14px;border-bottom:1px solid var(--border)">
      <span class="eyebrow">功能菜单</span>
    </div>
    <div style="flex:1;overflow-y:auto"><CategoryNav /></div>
  </div>
</template>

<script setup lang="ts">
import CategoryNav from './CategoryNav.vue'
</script>
