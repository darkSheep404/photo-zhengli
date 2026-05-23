<template>
  <div class="review-page">
    <div class="review-header">
      <button class="back-btn" @click="$router.back()">← 返回</button>
      <h2>确认操作</h2>
      <button class="clear-btn" @click="store.clearAll()">清除全部</button>
    </div>

    <!-- 删除列表 -->
    <section v-if="store.deleteList.length" class="section">
      <h3 class="section-title">
        🗑 待删除 ({{ store.deleteList.length }})
        <span class="space-info">释放 {{ formatBytes(store.totalSpaceToFree) }}</span>
      </h3>
      <div class="photo-grid">
        <div v-for="d in store.deleteList" :key="d.photo.id" class="grid-item delete">
          <img :src="d.photo.webPath" :alt="d.photo.filename" />
          <button class="remove-btn" @click="store.undoLast()">✕</button>
        </div>
      </div>
    </section>

    <!-- 移动列表 -->
    <section v-if="store.moveList.length" class="section">
      <h3 class="section-title">📁 待移动 ({{ store.moveList.length }})</h3>
      <div class="move-list">
        <div v-for="d in store.moveList" :key="d.photo.id" class="move-item">
          <img :src="d.photo.webPath" class="move-thumb" />
          <div class="move-info">
            <p>{{ d.photo.filename }}</p>
            <p class="move-target">→ {{ d.targetAlbum?.name }}</p>
          </div>
        </div>
      </div>
    </section>

    <div v-if="!store.deleteList.length && !store.moveList.length" class="empty">
      <p>没有待处理的操作</p>
    </div>

    <!-- 底部确认按钮 -->
    <div class="confirm-section">
      <button
        v-if="store.deleteList.length"
        class="confirm-btn danger"
        :disabled="executing"
        @click="executeDelete"
      >
        {{ executing ? '处理中...' : `确认删除 ${store.deleteList.length} 张照片` }}
      </button>
      <button class="confirm-btn secondary" @click="$router.push('/')">
        返回首页
      </button>
    </div>

    <!-- 完成弹窗 -->
    <div v-if="showResult" class="result-overlay" @click="goHome">
      <div class="result-card" @click.stop>
        <div class="result-icon">✅</div>
        <h3>整理完成</h3>
        <p>已移入最近删除 {{ deletedCount }} 张照片</p>
        <p class="freed-space">释放了 {{ formatBytes(freedBytes) }}</p>
        <p class="restore-hint">可在系统相册「最近删除」中恢复</p>
        <button class="confirm-btn" @click="goHome">完成</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCleanupStore } from '@/store/cleanupStore'
import { trashPhotos } from '@/composables/useMovePhoto'

const router = useRouter()
const store = useCleanupStore()

const executing = ref(false)
const showResult = ref(false)
const deletedCount = ref(0)
const freedBytes = ref(0)

async function executeDelete() {
  executing.value = true
  try {
    const uris = store.deleteList.map(d => d.photo.uri)
    const bytes = await trashPhotos(uris)
    deletedCount.value = uris.length
    freedBytes.value = bytes
    showResult.value = true
    // 清理已删除的条目
    store.decisions = store.decisions.filter(d => d.action !== 'delete')
  } catch (e) {
    console.error('删除失败:', e)
  } finally {
    executing.value = false
  }
}

function goHome() {
  store.clearAll()
  router.push('/')
}

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}
</script>

<style scoped>
.review-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
  padding-top: var(--safe-area-top);
  overflow-y: auto;
}

.review-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-md);
  border-bottom: 1px solid var(--color-separator);
  background: var(--color-surface);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
}

.review-header h2 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.back-btn, .clear-btn {
  font-size: var(--font-size-sm);
  color: var(--color-primary);
  font-weight: var(--font-weight-medium);
}

.section {
  padding: var(--space-md);
}

.section-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--space-md);
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.space-info {
  font-size: var(--font-size-sm);
  color: var(--color-danger);
  font-weight: var(--font-weight-regular);
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-xs);
}

.grid-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: var(--radius-xs);
  overflow: hidden;
}

.grid-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.grid-item.delete {
  opacity: 0.7;
}

.remove-btn {
  position: absolute;
  top: var(--space-xs);
  right: var(--space-xs);
  width: 22px;
  height: 22px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: var(--blur-sm);
  -webkit-backdrop-filter: var(--blur-sm);
  border-radius: var(--radius-full);
  font-size: 11px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.move-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.move-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-sm);
  border: 1px solid var(--color-border);
}

.move-thumb {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-xs);
  object-fit: cover;
}

.move-info p {
  font-size: var(--font-size-sm);
}

.move-target {
  color: var(--color-primary);
  margin-top: 2px;
}

.empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-secondary);
  font-size: var(--font-size-md);
}

.confirm-section {
  padding: var(--space-md);
  padding-bottom: calc(var(--safe-area-bottom) + var(--space-md));
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  margin-top: auto;
}

.confirm-btn {
  width: 100%;
  padding: var(--space-md);
  border-radius: var(--radius-md);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  background: var(--color-primary);
  color: white;
  transition: transform var(--transition-fast);
}

.confirm-btn:active {
  transform: scale(0.98);
}

.confirm-btn.danger {
  background: var(--color-danger);
}

.confirm-btn.secondary {
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
}

.confirm-btn:disabled {
  opacity: 0.6;
}

.result-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-card {
  background: var(--color-surface-solid);
  border-radius: var(--radius-xl);
  padding: var(--space-xl);
  text-align: center;
  max-width: 300px;
  width: 80%;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
}

.result-icon {
  font-size: 48px;
  margin-bottom: var(--space-md);
}

.result-card h3 {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--space-md);
}

.freed-space {
  color: var(--color-success);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  margin: var(--space-sm) 0;
}

.restore-hint {
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
  margin-bottom: var(--space-lg);
}
</style>
