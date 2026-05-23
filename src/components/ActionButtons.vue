<template>
  <div class="action-buttons">
    <button class="action-btn undo" :disabled="!canUndo" @click="$emit('undo')">
      <span class="btn-icon">↩</span>
      <span class="btn-label">撤销</span>
    </button>
    <button class="action-btn add-to" @click="$emit('addTo')">
      <span class="btn-icon">📁</span>
      <span class="btn-label">添加到</span>
    </button>
    <button class="action-btn delete" @click="$emit('delete')">
      <span class="btn-icon">🗑</span>
      <span class="btn-label">删除</span>
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  canUndo: boolean
}>()

defineEmits<{
  undo: []
  addTo: []
  delete: []
}>()
</script>

<style scoped>
.action-buttons {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 12px 24px;
  padding-bottom: calc(var(--safe-area-bottom) + 12px);
  background: var(--color-surface);
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  justify-content: center;
  background: var(--color-surface-2);
  transition: transform 0.15s, opacity 0.15s;
}

.action-btn:active {
  transform: scale(0.9);
}

.action-btn:disabled {
  opacity: 0.3;
}

.btn-icon {
  font-size: 24px;
}

.btn-label {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.action-btn.delete:active {
  background: rgba(231, 76, 60, 0.2);
}

.action-btn.add-to:active {
  background: rgba(74, 144, 217, 0.2);
}
</style>
