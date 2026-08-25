<template>
  <div class="albums-page">
    <div class="albums-header">
      <div>
        <h1>📁 相册浏览</h1>
        <p class="albums-subtitle">点击进入整理 · 长按管理是否排除</p>
      </div>
      <button class="sort-btn" @click="toggleSortMode">
        {{ sortMode === 'favorites' ? '★ 常用优先' : 'A-Z 名称' }}
      </button>
    </div>

    <div v-if="loading" class="loading-state">
      加载相册中...
    </div>

    <div v-else-if="albums.length === 0" class="empty-state">
      <p>暂无相册</p>
    </div>

    <template v-else>
      <div class="album-grid">
        <div
          v-for="album in sortedAlbums"
          :key="album.id"
          class="album-card glass"
          :class="{ excluded: checkExcluded(album.id) }"
          role="button"
          tabindex="0"
          @click="goCleanup(album)"
          @keydown.enter="goCleanup(album)"
          @contextmenu.prevent="openExcludeDialog(album)"
          @touchstart="startLongPress(album)"
          @touchend="cancelLongPress"
          @touchmove="cancelLongPress"
          @touchcancel="cancelLongPress"
        >
          <div class="album-icon">{{ checkExcluded(album.id) ? '🚫' : '📁' }}</div>
          <div class="album-info">
            <span class="album-name">{{ album.name }}</span>
            <span class="album-count">{{ album.count || '—' }} 张</span>
          </div>
          <button
            class="favorite-btn"
            :class="{ active: isFavorite(album.id) }"
            :aria-label="isFavorite(album.id) ? `取消常用 ${album.name}` : `设为常用 ${album.name}`"
            @click.stop="toggleAlbumFavorite(album)"
          >
            {{ isFavorite(album.id) ? '★' : '☆' }}
          </button>
          <span v-if="checkExcluded(album.id)" class="excluded-badge">已排除</span>
          <span v-else class="album-arrow">›</span>
        </div>
      </div>
    </template>

    <div v-if="targetAlbum" class="dialog-overlay" @click="closeExcludeDialog">
      <div class="exclude-dialog" @click.stop>
        <h3>{{ checkExcluded(targetAlbum.id) ? '恢复相册整理' : '排除该相册？' }}</h3>
        <p>
          {{ checkExcluded(targetAlbum.id)
            ? `“${targetAlbum.name}”将重新出现在全部照片整理中。`
            : `“${targetAlbum.name}”的照片将不再出现在全部照片整理中。` }}
        </p>
        <p class="dialog-hint">单独进入该相册时，始终可以继续整理。</p>
        <div class="dialog-actions">
          <button class="dialog-btn" @click="closeExcludeDialog">取消</button>
          <button class="dialog-btn primary" @click="confirmExcludeChange">
            {{ checkExcluded(targetAlbum.id) ? '恢复相册' : '确认排除' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="toastMsg" class="toast" @click="toastMsg = ''">{{ toastMsg }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAlbums } from '@/composables/useAlbums'
import { useCleanupStore } from '@/store/cleanupStore'
import { useExcludedAlbums } from '@/composables/useExcludedAlbums'
import { useFavoriteAlbums } from '@/composables/useFavoriteAlbums'
import type { Album } from '@/types/photo'

const router = useRouter()
const { albums, loading, loadAlbums } = useAlbums()
const store = useCleanupStore()
const { getExcludedAlbums, toggleExclude } = useExcludedAlbums()
const { getFavoriteIds, toggleFavorite } = useFavoriteAlbums()

const excludedList = ref(getExcludedAlbums())
const favoriteIds = ref(getFavoriteIds())
const toastMsg = ref('')
const sortMode = ref<'favorites' | 'name'>('favorites')
const targetAlbum = ref<Album | null>(null)
let longPressTimer: ReturnType<typeof setTimeout> | null = null
let longPressTriggered = false

onMounted(() => loadAlbums())

const sortedAlbums = computed(() => {
  const sorted = [...albums.value].sort((left, right) =>
    left.name.localeCompare(right.name, 'zh-CN')
  )

  if (sortMode.value === 'name') return sorted

  return sorted.sort((left, right) => {
    const leftFavorite = favoriteIds.value.has(left.id) ? 1 : 0
    const rightFavorite = favoriteIds.value.has(right.id) ? 1 : 0
    return rightFavorite - leftFavorite
  })
})

function checkExcluded(albumId: string): boolean {
  return excludedList.value.some(a => a.id === albumId)
}

function goCleanup(album: Album) {
  if (longPressTriggered) {
    longPressTriggered = false
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

function isFavorite(albumId: string): boolean {
  return favoriteIds.value.has(albumId)
}

function toggleSortMode() {
  sortMode.value = sortMode.value === 'favorites' ? 'name' : 'favorites'
}

function toggleAlbumFavorite(album: Album) {
  const nowFavorite = toggleFavorite(album.id, album.name)
  favoriteIds.value = getFavoriteIds()
  toastMsg.value = nowFavorite ? `已将“${album.name}”设为常用` : `已取消“${album.name}”常用`
  setTimeout(() => toastMsg.value = '', 2000)
}

function openExcludeDialog(album: Album) {
  longPressTriggered = true
  targetAlbum.value = album
}

function startLongPress(album: Album) {
  longPressTriggered = false
  longPressTimer = setTimeout(() => {
    openExcludeDialog(album)
  }, 600)
}

function cancelLongPress() {
  if (longPressTimer) {
    clearTimeout(longPressTimer)
    longPressTimer = null
  }
}

function closeExcludeDialog() {
  targetAlbum.value = null
}

function confirmExcludeChange() {
  if (!targetAlbum.value) return
  const album = targetAlbum.value
  const nowExcluded = toggleExclude(album.id, album.name)
  excludedList.value = getExcludedAlbums()
  toastMsg.value = nowExcluded ? `已排除“${album.name}”` : `已恢复“${album.name}”`
  targetAlbum.value = null
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
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-sm);
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

.sort-btn {
  flex-shrink: 0;
  padding: var(--space-xs) var(--space-sm);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface);
  color: var(--color-primary);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
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
  cursor: pointer;
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

.favorite-btn {
  flex: 0 0 36px;
  min-height: 36px;
  color: var(--color-text-tertiary);
  font-size: var(--font-size-xl);
}

.favorite-btn.active {
  color: var(--color-warning);
}

/* Excluded state */
.album-card.excluded {
  opacity: 0.5;
  border-style: dashed;
}

.excluded-badge {
  font-size: var(--font-size-xs);
  color: var(--color-danger);
  font-weight: var(--font-weight-medium);
  white-space: nowrap;
}

.dialog-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-lg);
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
}

.exclude-dialog {
  width: min(100%, 360px);
  padding: var(--space-lg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-surface-solid);
  box-shadow: var(--shadow-lg);
}

.exclude-dialog h3 {
  margin-bottom: var(--space-sm);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.exclude-dialog p {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.6;
}

.exclude-dialog .dialog-hint {
  margin-top: var(--space-xs);
  color: var(--color-text-tertiary);
  font-size: var(--font-size-xs);
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
  margin-top: var(--space-lg);
}

.dialog-btn {
  padding: var(--space-sm) var(--space-md);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface-2);
  font-size: var(--font-size-sm);
}

.dialog-btn.primary {
  border-color: var(--color-danger);
  background: var(--color-danger);
  color: #fff;
}

/* Toast */
.toast {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--color-surface-solid);
  color: var(--color-text);
  padding: 8px 20px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-md);
  font-size: var(--font-size-sm);
  z-index: 100;
  white-space: nowrap;
}
</style>
