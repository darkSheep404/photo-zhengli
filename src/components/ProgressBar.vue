<template>
  <div class="progress-bar">
    <button class="nav-btn" @click="$emit('back')">←</button>
    <span class="progress-text" @click="$emit('back')">
      {{ current }}/{{ total }} ›
    </span>
    <button class="month-btn" @click="$emit('monthClick')">
      {{ displayMonth }}
      <span class="filter-label">| 筛选 ›</span>
    </button>
    <span class="finish-btn" :class="{ active: hasDecisions }" @click="$emit('review')">
      结束整理 ›
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  current: number
  total: number
  deleteCount: number
  hasDecisions: boolean
  selectedMonth: string
  photoTime: number
}>()

defineEmits<{
  back: []
  monthClick: []
  review: []
}>()

const displayMonth = computed(() => {
  if (props.selectedMonth) return props.selectedMonth
  if (props.photoTime) {
    const d = new Date(props.photoTime)
    return `${d.getFullYear()}年${d.getMonth() + 1}月`
  }
  return ''
})
</script>

<style scoped>
.progress-bar {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  padding-top: calc(var(--safe-area-top) + var(--space-sm));
  background: var(--color-surface);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  border-bottom: 1px solid var(--color-separator);
}

.nav-btn {
  font-size: var(--font-size-lg);
  padding: var(--space-xs) var(--space-sm);
  color: var(--color-primary);
  border-radius: var(--radius-sm);
}

.nav-btn:active {
  background: var(--color-surface-2);
}

.progress-text {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  white-space: nowrap;
}

.month-btn {
  flex: 1;
  text-align: center;
  font-size: var(--font-size-sm);
  background: var(--color-surface-2);
  backdrop-filter: var(--blur-sm);
  -webkit-backdrop-filter: var(--blur-sm);
  padding: var(--space-xs) var(--space-md);
  border-radius: var(--radius-full);
  transition: background var(--transition-fast);
}

.month-btn:active {
  background: var(--color-surface-2-solid);
}

.filter-label {
  color: var(--color-text-tertiary);
  margin-left: var(--space-xs);
}

.finish-btn {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  white-space: nowrap;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
  cursor: pointer;
}

.finish-btn:active {
  opacity: 0.7;
}

.finish-btn.active {
  color: white;
  background: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}
</style>
