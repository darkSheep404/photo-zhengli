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
  padding: var(--space-md) var(--space-lg);
  padding-bottom: calc(var(--safe-area-bottom) + var(--space-md));
  background: var(--color-surface);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  border-top: 1px solid var(--color-separator);
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-xs);
  width: 72px;
  height: 72px;
  border-radius: var(--radius-full);
  justify-content: center;
  background: var(--color-surface-2);
  backdrop-filter: var(--blur-sm);
  -webkit-backdrop-filter: var(--blur-sm);
  transition: transform var(--transition-fast), background var(--transition-fast);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

.action-btn:active {
  transform: scale(0.88);
}

.action-btn:disabled {
  opacity: 0.3;
}

.btn-icon {
  font-size: 22px;
}

.btn-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.action-btn.delete:active {
  background: rgba(var(--color-danger-rgb), 0.3);
  border-color: var(--color-danger);
}

.action-btn.add-to:active {
  background: rgba(var(--color-primary-rgb), 0.3);
  border-color: var(--color-primary);
}
</style>
