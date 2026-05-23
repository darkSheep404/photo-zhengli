<template>
  <div class="thumbnail-strip" ref="stripRef"
    @touchstart="onStripTouchStart"
    @touchmove="onStripTouchMove"
    @touchend="onStripTouchEnd"
  >
    <div class="strip-inner" :style="{ transform: `translateX(${scrollOffset}px)`, transition: isDragging ? 'none' : 'transform var(--transition-normal)' }">
      <div
        v-for="(photo, index) in photos"
        :key="photo.id"
        class="thumb-item"
        :class="{
          active: index === currentIndex,
          'marked-delete': getDecision(photo.id)?.action === 'delete',
          'marked-move': getDecision(photo.id)?.action === 'move',
          'marked-keep': getDecision(photo.id)?.action === 'keep',
        }"
        @click="$emit('select', index)"
      >
        <img :src="photo.webPath" :alt="photo.filename" loading="lazy" />
        <span v-if="getDecision(photo.id)?.action === 'delete'" class="thumb-label delete">待删除</span>
        <span v-else-if="getDecision(photo.id)?.action === 'keep'" class="thumb-label keep">保留</span>
        <span v-else-if="getDecision(photo.id)?.action === 'move'" class="thumb-label move">已分类</span>
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
const isDragging = ref(false)
const dragStartX = ref(0)
const dragStartOffset = ref(0)
const THUMB_WIDTH = 68 // 60px thumb + 8px gap

watch(() => props.currentIndex, scrollToActive)

onMounted(() => {
  scrollToActive()
})

function scrollToActive() {
  if (!stripRef.value || isDragging.value) return
  const containerWidth = stripRef.value.clientWidth
  const targetX = props.currentIndex * THUMB_WIDTH
  const center = containerWidth / 2 - THUMB_WIDTH / 2
  scrollOffset.value = Math.min(0, center - targetX)
}

function getMinOffset(): number {
  if (!stripRef.value) return 0
  const totalWidth = props.photos.length * THUMB_WIDTH
  const containerWidth = stripRef.value.clientWidth
  return Math.min(0, containerWidth - totalWidth)
}

function onStripTouchStart(e: TouchEvent) {
  isDragging.value = true
  dragStartX.value = e.touches[0].clientX
  dragStartOffset.value = scrollOffset.value
}

function onStripTouchMove(e: TouchEvent) {
  if (!isDragging.value) return
  e.preventDefault()
  const dx = e.touches[0].clientX - dragStartX.value
  const newOffset = dragStartOffset.value + dx
  // Clamp within bounds
  scrollOffset.value = Math.max(getMinOffset(), Math.min(0, newOffset))
}

function onStripTouchEnd() {
  isDragging.value = false
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

.thumb-item.marked-keep::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(52, 199, 89, 0.25);
}

.thumb-label {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  font-size: 8px;
  text-align: center;
  color: white;
  padding: 1px 0;
}

.thumb-label.delete {
  background: rgba(var(--color-danger-rgb), 0.85);
}

.thumb-label.keep {
  background: rgba(52, 199, 89, 0.85);
}

.thumb-label.move {
  background: rgba(var(--color-primary-rgb), 0.85);
}
</style>
