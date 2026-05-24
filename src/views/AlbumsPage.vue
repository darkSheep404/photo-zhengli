<template>
  <div class="albums-page">
    <div class="albums-header">
      <h1>📁 相册浏览</h1>
      <p class="albums-subtitle">点击进入清理 · 长按排除/恢复</p>
    </div>

    <div v-if="loading" class="loading-state">
      加载相册中...
    </div>

    <div v-else-if="albums.length === 0" class="empty-state">
      <p>暂无相册</p>
    </div>

    <template v-else>
      <!-- 已排除相册提示 -->
      <div v-if="excludedList.length > 0" class="excluded-section">
        <h3 class="section-label">已排除 ({{ excludedList.length }})</h3>
        <div class="excluded-chips">
          <span
            v-for="ex in excludedList"
            :key="ex.id"
            class="excluded-chip"
            @click="handleRestore(ex.id, ex.name)"
          >
            🚫 {{ ex.name }} ✕
          </span>
        </div>
      </div>

      <div class="album-grid">
        <button
          v-for="album in albums"
          :key="album.id"
          class="album-card glass"
          :class="{ excluded: checkExcluded(album.id) }"
          @click="goCleanup(album)"
          @contextmenu.prevent="handleLongPress(album)"
          @touchstart="startLongPress(album)"
          @touchend="cancelLongPress"
          @touchmove="cancelLongPress"
        >
          <div class="album-icon">{{ checkExcluded(album.id) ? '🚫' : '📁' }}</div>
          <div class="album-info">
            <span class="album-name">{{ album.name }}</span>
            <span class="album-count">{{ album.count || '—' }} 张</span>
          </div>
          <span v-if="checkExcluded(album.id)" class="excluded-badge">已排除</span>
          <span v-else class="album-arrow">›</span>
        </button>
      </div>
    </template>

    <!-- Toast -->
    <div v-if="toastMsg" class="toast" @click="toastMsg = ''">{{ toastMsg }}</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAlbums } from '@/composables/useAlbums'
import { useCleanupStore } from '@/store/cleanupStore'
import { useExcludedAlbums } from '@/composables/useExcludedAlbums'
import type { Album } from '@/types/photo'

const router = useRouter()
const { albums, loading, loadAlbums } = useAlbums()
const store = useCleanupStore()
const { getExcludedAlbums, isExcluded, toggleExclude } = useExcludedAlbums()

const excludedList = ref(getExcludedAlbums())
const toastMsg = ref('')
let longPressTimer: ReturnType<typeof setTimeout> | null = null
let longPressTriggered = false

onMounted(() => loadAlbums())

function checkExcluded(albumId: string): boolean {
  return excludedList.value.some(a => a.id === albumId)
}

function goCleanup(album: Album) {
  if (longPressTriggered) {
    longPressTriggered = false
    return
  }
  if (checkExcluded(album.id)) {
    toastMsg.value = `"${album.name}" 已排除，长按可恢复`
    setTimeout(() => toastMsg.value = '', 2000)
    return
  }
  store.cleanupConfig = {
    scope: 'album',
    albumIds: [album.id],
    sortOrder: store.cleanupConfig.sortOrder,
    batchSize: 50,
  }
  router.push('/cleanup/session')
}

function handleLongPress(album: Album) {
  longPressTriggered = true
  const nowExcluded = toggleExclude(album.id, album.name)
  excludedList.value = getExcludedAlbums()
  toastMsg.value = nowExcluded ? `已排除 "${album.name}"` : `已恢复 "${album.name}"`
  setTimeout(() => toastMsg.value = '', 2000)
}

function startLongPress(album: Album) {
  longPressTriggered = false
  longPressTimer = setTimeout(() => {
    handleLongPress(album)
  }, 600)
}

function cancelLongPress() {
  if (longPressTimer) {
    clearTimeout(longPressTimer)
    longPressTimer = null
  }
}

function handleRestore(id: string, name: string) {
  toggleExclude(id, name)
  excludedList.value = getExcludedAlbums()
  toastMsg.value = `已恢复 "${name}"`
  setTimeout(() => toastMsg.value = '', 2000)
}
</script>

<style scoped>
.albums-page {
  height: 100%;
  overflow-y: auto;
  padding: var(--space-lg);
  padding-top: calc(var(--safe-area-top) + var(--space-lg));
  background: var(--color-bg);
}

.albums-header {
  margin-bottom: var(--space-xl);
}

.albums-header h1 {
  font-size: var(--font-size-xxl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--space-xs);
}

.albums-subtitle {
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
}

.loading-state, .empty-state {
  text-align: center;
  color: var(--color-text-secondary);
  padding: var(--space-xxl) 0;
}

.album-grid {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.album-card {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border);
  text-align: left;
  box-shadow: var(--shadow-sm);
  transition: transform var(--transition-fast);
}

.album-card:active {
  transform: scale(0.98);
}

.album-icon {
  font-size: 28px;
}

.album-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.album-name {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
}

.album-count {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.album-arrow {
  font-size: var(--font-size-xl);
  color: var(--color-text-tertiary);
}

/* Excluded state */
.album-card.excluded {
  opacity: 0.5;
  border-style: dashed;
}

.excluded-badge {
  font-size: var(--font-size-xs);
  color: var(--color-danger, #FF3B30);
  font-weight: var(--font-weight-medium);
  white-space: nowrap;
}

.excluded-section {
  margin-bottom: var(--space-md);
}

.excluded-section .section-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-weight: var(--font-weight-medium);
  margin-bottom: var(--space-xs);
  padding-left: var(--space-xs);
}

.excluded-chips {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-xs);
}

.excluded-chip {
  font-size: var(--font-size-xs);
  padding: 4px 10px;
  border-radius: var(--radius-full);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  cursor: pointer;
}

.excluded-chip:active {
  background: var(--color-surface-2);
}

/* Toast */
.toast {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: var(--font-size-sm);
  z-index: 100;
  white-space: nowrap;
}
</style>
