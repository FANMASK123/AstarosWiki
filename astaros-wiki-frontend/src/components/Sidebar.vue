<template>
  <div style="height:100%;display:flex;flex-direction:column;position:relative">
    <div style="padding:10px 14px;border-bottom:1px solid var(--border)">
      <span class="eyebrow">分类导航</span>
    </div>
    <CategoryNav style="flex:1;overflow:hidden" />
  </div>
</template>

<script setup lang="ts">
import CategoryNav from './CategoryNav.vue'
</script>
