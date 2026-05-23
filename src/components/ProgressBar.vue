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
    <span class="delete-count" :class="{ active: deleteCount > 0 }">
      待删除({{ deleteCount }})
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  current: number
  total: number
  deleteCount: number
  selectedMonth: string
}>()

defineEmits<{
  back: []
  monthClick: []
}>()

const displayMonth = computed(() => {
  if (props.selectedMonth) return props.selectedMonth
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月`
})
</script>

<style scoped>
.progress-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  padding-top: calc(var(--safe-area-top) + 10px);
  background: var(--color-surface);
}

.nav-btn {
  font-size: 18px;
  padding: 4px 8px;
}

.progress-text {
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
}

.month-btn {
  flex: 1;
  text-align: center;
  font-size: 14px;
  background: var(--color-surface-2);
  padding: 6px 12px;
  border-radius: 20px;
}

.filter-label {
  color: var(--color-text-secondary);
  margin-left: 4px;
}

.delete-count {
  font-size: 14px;
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.delete-count.active {
  color: var(--color-danger);
  font-weight: 600;
}
</style>
