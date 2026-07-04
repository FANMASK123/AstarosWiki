<template>
  <button class="btn btn-primary" style="position:fixed;bottom:24px;left:24px;z-index:200;width:44px;height:44px;border-radius:50%;padding:0;justify-content:center;font-size:18px;display:none" :style="{display:visible?'flex':'none'}" @click="$emit('toggle')">☰</button>
</template>
<script setup lang="ts">
defineProps<{visible:boolean}>()
defineEmits(['toggle'])
</script>
<style scoped>
@media(max-width:1024px){button{display:flex!important}}
</style>
