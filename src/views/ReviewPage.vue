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
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border);
}

.review-header h2 {
  font-size: 17px;
}

.back-btn, .clear-btn {
  font-size: 14px;
  color: var(--color-primary);
}

.section {
  padding: 16px;
}

.section-title {
  font-size: 15px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.space-info {
  font-size: 13px;
  color: var(--color-danger);
  font-weight: 400;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
}

.grid-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 4px;
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
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  font-size: 12px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.move-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.move-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--color-surface);
  border-radius: 8px;
  padding: 8px;
}

.move-thumb {
  width: 48px;
  height: 48px;
  border-radius: 4px;
  object-fit: cover;
}

.move-info p {
  font-size: 13px;
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
}

.confirm-section {
  padding: 16px;
  padding-bottom: calc(var(--safe-area-bottom) + 16px);
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: auto;
}

.confirm-btn {
  width: 100%;
  padding: 14px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: var(--color-primary);
  color: white;
}

.confirm-btn.danger {
  background: var(--color-danger);
}

.confirm-btn.secondary {
  background: var(--color-surface);
  color: var(--color-text);
}

.confirm-btn:disabled {
  opacity: 0.6;
}

.result-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
}

.result-card {
  background: var(--color-surface);
  border-radius: 20px;
  padding: 32px;
  text-align: center;
  max-width: 300px;
  width: 80%;
}

.result-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.result-card h3 {
  font-size: 20px;
  margin-bottom: 12px;
}

.freed-space {
  color: var(--color-success);
  font-size: 18px;
  font-weight: 600;
  margin: 8px 0;
}

.restore-hint {
  color: var(--color-text-secondary);
  font-size: 12px;
  margin-bottom: 20px;
}
</style>
