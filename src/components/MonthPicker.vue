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
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
}

.picker {
  width: 100%;
  max-height: 50vh;
  background: var(--color-surface);
  border-radius: 20px 20px 0 0;
  padding: 12px 16px;
  padding-bottom: calc(var(--safe-area-bottom) + 16px);
  overflow-y: auto;
  animation: slideUp 0.25s ease-out;
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.picker-handle {
  width: 36px;
  height: 4px;
  background: var(--color-border);
  border-radius: 2px;
  margin: 0 auto 12px;
}

.picker h3 {
  font-size: 17px;
  margin-bottom: 16px;
}

.month-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 16px;
}

.month-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 8px;
  background: var(--color-surface-2);
  border-radius: 10px;
  transition: background 0.15s;
}

.month-item.active {
  background: var(--color-primary);
}

.month-label {
  font-size: 13px;
}

.month-count {
  font-size: 11px;
  color: var(--color-text-secondary);
}

.month-item.active .month-count {
  color: rgba(255, 255, 255, 0.7);
}

.clear-filter {
  width: 100%;
  padding: 12px;
  text-align: center;
  font-size: 14px;
  color: var(--color-primary);
}
</style>
