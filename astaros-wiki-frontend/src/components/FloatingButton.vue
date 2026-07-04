<template>
  <div class="floating-button-container">
    <div
      class="floating-button"
      :class="{ 'floating-button-active': visible }"
      @click="$emit('toggle')"
    >
      <el-icon :class="{ rotate: visible }">
        <Menu />
      </el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Menu } from '@element-plus/icons-vue'

defineProps<{
  visible: boolean
}>()

defineEmits<{
  toggle: []
}>()
</script>

<style scoped>
.floating-button-container {
  position: fixed;
  bottom: 32px;
  right: 16px;
  z-index: 500;
  display: none;
}

.floating-button {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(
    135deg,
    var(--classic-accent-primary),
    var(--classic-accent-secondary)
  );
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(139, 115, 85, 0.4);
  transition: all 0.3s ease;
}

.floating-button:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(139, 115, 85, 0.5);
}

.floating-button-active {
  transform: rotate(45deg);
}

.floating-button-active:hover {
  transform: scale(1.1) rotate(45deg);
}

.floating-button .rotate {
  transition: all 0.3s ease;
}

.floating-button-active .rotate {
  transform: rotate(90deg);
}

@media (max-width: 768px) {
  .floating-button-container {
    display: block;
  }
}
</style>
