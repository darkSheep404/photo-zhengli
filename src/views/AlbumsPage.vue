<template>
  <div class="albums-page">
    <div class="albums-header">
      <h1>📁 相册浏览</h1>
      <p class="albums-subtitle">选择相册进入清理</p>
    </div>

    <div v-if="loading" class="loading-state">
      加载相册中...
    </div>

    <div v-else-if="albums.length === 0" class="empty-state">
      <p>暂无相册</p>
    </div>

    <div v-else class="album-grid">
      <button
        v-for="album in albums"
        :key="album.id"
        class="album-card glass"
        @click="goCleanup(album)"
      >
        <div class="album-icon">📁</div>
        <div class="album-info">
          <span class="album-name">{{ album.name }}</span>
          <span class="album-count">{{ album.count || '—' }} 张</span>
        </div>
        <span class="album-arrow">›</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAlbums } from '@/composables/useAlbums'
import { useCleanupStore } from '@/store/cleanupStore'
import type { Album } from '@/types/photo'

const router = useRouter()
const { albums, loading, loadAlbums } = useAlbums()
const store = useCleanupStore()

onMounted(() => loadAlbums())

function goCleanup(album: Album) {
  store.cleanupConfig = {
    scope: 'album',
    albumIds: [album.id],
    sortOrder: 'oldest',
    batchSize: 50,
  }
  router.push('/cleanup/session')
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
</style>
