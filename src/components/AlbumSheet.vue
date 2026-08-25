<template>
  <div class="sheet-overlay" @click="$emit('close')">
    <div class="sheet" @click.stop>
      <div class="sheet-handle"></div>
      <h3 class="sheet-title">添加到相册</h3>

      <button class="create-album-btn" @click="showCreate = true">
        <span class="create-icon">+</span>
        <span>新建相册</span>
      </button>

      <section v-if="favoriteAlbums.length > 0" class="album-group">
        <h4 class="group-title">常用相册</h4>
        <div class="album-list">
          <div
            v-for="album in favoriteAlbums"
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
            <span class="favorite-mark">★</span>
          </div>
        </div>
      </section>

      <section class="album-group">
        <h4 v-if="favoriteAlbums.length > 0" class="group-title">全部相册</h4>
        <div class="album-list">
        <div
          v-for="album in otherAlbums"
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
      </section>

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
import { computed, ref } from 'vue'
import type { Album } from '@/types/photo'
import { useFavoriteAlbums } from '@/composables/useFavoriteAlbums'

const props = defineProps<{
  albums: Album[]
}>()

const emit = defineEmits<{
  select: [album: Album]
  create: [name: string]
  close: []
}>()

const showCreate = ref(false)
const newAlbumName = ref('')
const { getFavoriteIds } = useFavoriteAlbums()
const favoriteIds = getFavoriteIds()

const sortedAlbums = computed(() =>
  [...props.albums].sort((left, right) => left.name.localeCompare(right.name, 'zh-CN'))
)
const favoriteAlbums = computed(() =>
  sortedAlbums.value.filter(album => favoriteIds.has(album.id))
)
const otherAlbums = computed(() =>
  sortedAlbums.value.filter(album => !favoriteIds.has(album.id))
)

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
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: var(--blur-sm);
  -webkit-backdrop-filter: var(--blur-sm);
  display: flex;
  align-items: flex-end;
}

.sheet {
  width: 100%;
  max-height: 60vh;
  background: var(--color-surface-solid);
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
  padding: var(--space-md);
  padding-bottom: calc(var(--safe-area-bottom) + var(--space-md));
  overflow-y: auto;
  animation: slideUp 0.3s var(--transition-slow);
  box-shadow: var(--shadow-lg);
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.sheet-handle {
  width: 36px;
  height: 4px;
  background: var(--color-text-tertiary);
  border-radius: var(--radius-full);
  margin: 0 auto var(--space-md);
}

.sheet-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--space-md);
}

.create-album-btn {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  width: 100%;
  padding: var(--space-md);
  background: var(--color-surface-2);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-md);
  font-size: var(--font-size-md);
  transition: background var(--transition-fast);
}

.create-album-btn:active {
  background: var(--color-surface-2-solid);
}

.create-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
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
  gap: 2px;
}

.album-group + .album-group {
  margin-top: var(--space-md);
}

.group-title {
  margin: 0 0 var(--space-xs) var(--space-xs);
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
}

.album-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-sm);
  transition: background var(--transition-fast);
}

.album-item:active {
  background: var(--color-surface-2);
}

.album-cover {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
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
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.album-name {
  font-size: var(--font-size-md);
}

.album-count {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

.favorite-mark {
  color: var(--color-warning);
  font-size: var(--font-size-lg);
}

.create-dialog {
  margin-top: var(--space-md);
  padding: var(--space-md);
  background: var(--color-surface-2);
  border-radius: var(--radius-md);
}

.create-input {
  width: 100%;
  padding: var(--space-sm) var(--space-md);
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  color: var(--color-text);
  font-size: var(--font-size-md);
  font-family: var(--font-family);
  outline: none;
  margin-bottom: var(--space-md);
  transition: border-color var(--transition-fast);
}

.create-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.15);
}

.create-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-md);
}

.create-actions button {
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  transition: background var(--transition-fast);
}

.create-actions .primary {
  background: var(--color-primary);
  color: white;
  font-weight: var(--font-weight-semibold);
}

.create-actions .primary:disabled {
  opacity: 0.5;
}
</style>
