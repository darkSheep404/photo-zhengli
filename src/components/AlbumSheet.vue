<template>
  <div class="sheet-overlay" @click="$emit('close')">
    <div class="sheet" @click.stop>
      <div class="sheet-handle"></div>
      <h3 class="sheet-title">添加到相册</h3>

      <button class="create-album-btn" @click="showCreate = true">
        <span class="create-icon">+</span>
        <span>新建相册</span>
      </button>

      <div class="album-list">
        <div
          v-for="album in albums"
          :key="album.id"
          class="album-item"
          @click="$emit('select', album)"
        >
          <div class="album-cover">
            <span v-if="!album.coverUri">📁</span>
            <img v-else :src="album.coverUri" alt="" />
          </div>
          <div class="album-info">
            <span class="album-name">{{ album.name }}</span>
            <span class="album-count">{{ album.count }} 张</span>
          </div>
        </div>
      </div>

      <!-- 新建相册对话框 -->
      <div v-if="showCreate" class="create-dialog">
        <input
          v-model="newAlbumName"
          placeholder="输入相册名称"
          class="create-input"
          autofocus
          @keyup.enter="confirmCreate"
        />
        <div class="create-actions">
          <button @click="showCreate = false">取消</button>
          <button class="primary" :disabled="!newAlbumName.trim()" @click="confirmCreate">
            创建并移动
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Album } from '@/types/photo'

defineProps<{
  albums: Album[]
}>()

const emit = defineEmits<{
  select: [album: Album]
  create: [name: string]
  close: []
}>()

const showCreate = ref(false)
const newAlbumName = ref('')

function confirmCreate() {
  const name = newAlbumName.value.trim()
  if (name) {
    emit('create', name)
    newAlbumName.value = ''
    showCreate.value = false
  }
}
</script>

<style scoped>
.sheet-overlay {
  position: fixed;
  inset: 0;
  z-index: 50;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
}

.sheet {
  width: 100%;
  max-height: 60vh;
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

.sheet-handle {
  width: 36px;
  height: 4px;
  background: var(--color-border);
  border-radius: 2px;
  margin: 0 auto 12px;
}

.sheet-title {
  font-size: 17px;
  margin-bottom: 16px;
}

.create-album-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px;
  background: var(--color-surface-2);
  border-radius: 12px;
  margin-bottom: 12px;
  font-size: 15px;
}

.create-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.album-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.album-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  transition: background 0.15s;
}

.album-item:active {
  background: var(--color-surface-2);
}

.album-cover {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: var(--color-surface-2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  overflow: hidden;
}

.album-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.album-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.album-name {
  font-size: 15px;
}

.album-count {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.create-dialog {
  margin-top: 16px;
  padding: 16px;
  background: var(--color-surface-2);
  border-radius: 12px;
}

.create-input {
  width: 100%;
  padding: 10px 12px;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  color: var(--color-text);
  font-size: 15px;
  outline: none;
  margin-bottom: 12px;
}

.create-input:focus {
  border-color: var(--color-primary);
}

.create-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.create-actions button {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
}

.create-actions .primary {
  background: var(--color-primary);
  color: white;
}

.create-actions .primary:disabled {
  opacity: 0.5;
}
</style>
