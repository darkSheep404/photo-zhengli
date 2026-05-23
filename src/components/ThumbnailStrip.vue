<template>
  <div class="thumbnail-strip" ref="stripRef">
    <div class="strip-inner" :style="{ transform: `translateX(${scrollOffset}px)` }">
      <div
        v-for="(photo, index) in photos"
        :key="photo.id"
        class="thumb-item"
        :class="{
          active: index === currentIndex,
          'marked-delete': getDecision(photo.id)?.action === 'delete',
          'marked-move': getDecision(photo.id)?.action === 'move',
        }"
        @click="$emit('select', index)"
      >
        <img :src="photo.webPath" :alt="photo.filename" loading="lazy" />
        <span v-if="getDecision(photo.id)?.action === 'delete'" class="thumb-label">待删除</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import type { Photo, PhotoDecision } from '@/types/photo'

const props = defineProps<{
  photos: Photo[]
  currentIndex: number
  getDecision: (id: string) => PhotoDecision | undefined
}>()

defineEmits<{
  select: [index: number]
}>()

const stripRef = ref<HTMLElement>()
const scrollOffset = ref(0)
const THUMB_WIDTH = 68 // 60px thumb + 8px gap

watch(() => props.currentIndex, scrollToActive)

onMounted(() => {
  scrollToActive()
})

function scrollToActive() {
  if (!stripRef.value) return
  const containerWidth = stripRef.value.clientWidth
  const targetX = props.currentIndex * THUMB_WIDTH
  const center = containerWidth / 2 - THUMB_WIDTH / 2
  scrollOffset.value = Math.min(0, center - targetX)
}
</script>

<style scoped>
.thumbnail-strip {
  width: 100%;
  overflow: hidden;
  padding: var(--space-sm) 0;
  background: var(--color-surface);
  backdrop-filter: var(--blur-sm);
  -webkit-backdrop-filter: var(--blur-sm);
}

.strip-inner {
  display: flex;
  gap: var(--space-sm);
  padding: 0 var(--space-md);
  transition: transform var(--transition-normal);
}

.thumb-item {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  border: 2px solid transparent;
  position: relative;
  transition: border-color var(--transition-fast), transform var(--transition-fast);
}

.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-item.active {
  border-color: var(--color-primary);
  transform: scale(1.08);
  box-shadow: var(--shadow-glow);
}

.thumb-item.marked-delete {
  opacity: 0.5;
}

.thumb-item.marked-delete::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(var(--color-danger-rgb), 0.4);
}

.thumb-item.marked-move::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(var(--color-primary-rgb), 0.3);
}

.thumb-label {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  font-size: 8px;
  text-align: center;
  background: rgba(var(--color-danger-rgb), 0.85);
  color: white;
  padding: 1px 0;
}
</style>
