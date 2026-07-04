<template>
  <Transition name="page">
    <div v-if="visible" style="position:fixed;inset:0;z-index:150;background:var(--bg);padding:80px 24px 24px;overflow-y:auto" @click.self="$emit('close')">
      <button class="btn btn-ghost" style="position:absolute;top:16px;right:16px" @click="$emit('close')">✕</button>
      <CategoryNav />
    </div>
  </Transition>
</template>
<script setup lang="ts">
import CategoryNav from './CategoryNav.vue'
defineProps<{visible:boolean}>()
defineEmits(['close'])
</script>
