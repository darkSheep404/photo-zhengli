<template>
  <div class="picker-overlay" @click="$emit('close')">
    <div class="picker" @click.stop>
      <div class="picker-handle"></div>
      <h3>选择月份</h3>
      <div class="month-grid">
        <button
          v-for="month in months"
          :key="month.key"
          class="month-item"
          :class="{ active: month.key === selected }"
          @click="$emit('select', month.key)"
        >
          <span class="month-label">{{ month.label }}</span>
          <span class="month-count">{{ month.count }} 张</span>
        </button>
      </div>
      <button class="clear-filter" @click="$emit('select', '')">
        清除筛选（显示全部）
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Photo } from '@/types/photo'

const props = defineProps<{
  photos: Photo[]
  selected: string
}>()

defineEmits<{
  select: [month: string]
  close: []
}>()

const months = computed(() => {
  const map = new Map<string, number>()
  for (const photo of props.photos) {
    const d = new Date(photo.createdAt)
    const key = `${d.getFullYear()}年${d.getMonth() + 1}月`
    map.set(key, (map.get(key) ?? 0) + 1)
  }
  return Array.from(map.entries())
    .map(([key, count]) => ({ key, label: key, count }))
    .sort((a, b) => b.key.localeCompare(a.key))
})
</script>

<style scoped>
.picker-overlay {
  position: fixed;
  inset: 0;
  z-index: 50;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: var(--blur-sm);
  -webkit-backdrop-filter: var(--blur-sm);
  display: flex;
  align-items: flex-end;
}

.picker {
  width: 100%;
  max-height: 50vh;
  background: var(--color-surface-solid);
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
  padding: var(--space-md);
  padding-bottom: calc(var(--safe-area-bottom) + var(--space-md));
  overflow-y: auto;
  animation: slideUp 0.3s ease-out;
  box-shadow: var(--shadow-lg);
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.picker-handle {
  width: 36px;
  height: 4px;
  background: var(--color-text-tertiary);
  border-radius: var(--radius-full);
  margin: 0 auto var(--space-md);
}

.picker h3 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--space-md);
}

.month-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-sm);
  margin-bottom: var(--space-md);
}

.month-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-md) var(--space-sm);
  background: var(--color-surface-2);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  border: 1px solid transparent;
}

.month-item:active {
  transform: scale(0.96);
}

.month-item.active {
  background: var(--color-primary);
  box-shadow: var(--shadow-glow);
}

.month-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.month-count {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.month-item.active .month-count {
  color: rgba(255, 255, 255, 0.7);
}

.clear-filter {
  width: 100%;
  padding: var(--space-md);
  text-align: center;
  font-size: var(--font-size-sm);
  color: var(--color-primary);
  font-weight: var(--font-weight-medium);
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.clear-filter:active {
  background: var(--color-surface-2);
}
</style>
