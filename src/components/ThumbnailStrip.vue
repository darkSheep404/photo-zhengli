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
  padding: 8px 0;
  background: var(--color-surface);
}

.strip-inner {
  display: flex;
  gap: 8px;
  padding: 0 16px;
  transition: transform 0.25s ease-out;
}

.thumb-item {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  border: 2px solid transparent;
  position: relative;
  transition: border-color 0.15s;
}

.thumb-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-item.active {
  border-color: var(--color-primary);
}

.thumb-item.marked-delete {
  opacity: 0.5;
}

.thumb-item.marked-delete::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(231, 76, 60, 0.4);
}

.thumb-item.marked-move::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(74, 144, 217, 0.3);
}

.thumb-label {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  font-size: 9px;
  text-align: center;
  background: rgba(231, 76, 60, 0.8);
  color: white;
  padding: 1px 0;
}
</style>
