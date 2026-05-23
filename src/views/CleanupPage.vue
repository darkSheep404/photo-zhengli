<template>
  <div class="cleanup-page">
    <!-- 扫描加载中 -->
    <div v-if="loading" class="scan-overlay">
      <div class="scan-box glass">
        <div class="scan-spinner"></div>
        <p class="scan-title">正在扫描照片...</p>
        <p class="scan-hint">{{ scanHint }}</p>
      </div>
    </div>

    <!-- 调试信息浮层 -->
    <div v-if="debugMsg" class="debug-toast" @click="debugMsg = ''">
      <pre>{{ debugMsg }}</pre>
    </div>

    <!-- 顶部信息栏 -->
    <ProgressBar
      v-if="!loading"
      :current="currentIndex + 1"
      :total="photos.length"
      :delete-count="store.deleteCount"
      :selected-month="store.selectedMonth"
      @back="goBack"
      @month-click="showMonthPicker = true"
    />

    <!-- 中间大图区域 -->
    <div v-if="!loading" class="photo-area"
      @touchstart="onTouchStart"
      @touchend="onTouchEnd"
      @click="showDetail = true"
    >
      <Transition :name="slideDirection" mode="out-in">
        <div v-if="currentPhoto" :key="currentPhoto.id" class="photo-container">
          <img
            :src="currentPhoto.webPath"
            :alt="currentPhoto.filename"
            class="main-photo"
            draggable="false"
          />
          <div v-if="currentDecision" class="decision-badge" :class="currentDecision.action">
            {{ currentDecision.action === 'delete' ? '待删除' : `移动到 ${currentDecision.targetAlbum?.name}` }}
          </div>
        </div>
      </Transition>
      <div v-if="!currentPhoto" class="empty-state">
        <p>没有照片了</p>
      </div>
    </div>

    <!-- 底部缩略图条 -->
    <ThumbnailStrip
      :photos="photos"
      :current-index="currentIndex"
      :get-decision="store.getDecision"
      @select="goToPhoto"
    />

    <!-- 底部操作按钮 -->
    <ActionButtons
      :can-undo="store.decisions.length > 0"
      @undo="handleUndo"
      @add-to="showAlbumSheet = true"
      @delete="handleDelete"
    />

    <!-- 相册选择 Bottom Sheet -->
    <AlbumSheet
      v-if="showAlbumSheet"
      :albums="albums"
      @select="handleMoveToAlbum"
      @create="handleCreateAlbum"
      @close="showAlbumSheet = false"
    />

    <!-- 月份选择器 -->
    <MonthPicker
      v-if="showMonthPicker"
      :photos="photos"
      :selected="store.selectedMonth"
      @select="handleMonthSelect"
      @close="showMonthPicker = false"
    />

    <!-- 照片详情 -->
    <div v-if="showDetail && currentPhoto" class="detail-overlay" @click="showDetail = false">
      <img :src="currentPhoto.webPath" class="detail-photo" />
      <div class="detail-info" @click.stop>
        <p>{{ currentPhoto.filename }}</p>
        <p>{{ formatDate(currentPhoto.createdAt) }}</p>
        <p>{{ formatBytes(currentPhoto.size) }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { usePhotos } from '@/composables/usePhotos'
import { useAlbums } from '@/composables/useAlbums'
import { useCleanupStore } from '@/store/cleanupStore'
import { movePhotoToAlbum } from '@/composables/useMovePhoto'
import type { Album } from '@/types/photo'
import ProgressBar from '@/components/ProgressBar.vue'
import ThumbnailStrip from '@/components/ThumbnailStrip.vue'
import ActionButtons from '@/components/ActionButtons.vue'
import AlbumSheet from '@/components/AlbumSheet.vue'
import MonthPicker from '@/components/MonthPicker.vue'

const router = useRouter()
const store = useCleanupStore()
const { photos, loading, currentIndex, currentPhoto, loadPhotos, loadPhotosWithConfig, goToIndex, nextPhoto, prevPhoto } = usePhotos()
const { albums, loadAlbums, createAlbum } = useAlbums()

const showAlbumSheet = ref(false)
const showMonthPicker = ref(false)
const showDetail = ref(false)
const touchStartX = ref(0)
const touchStartY = ref(0)
const slideDirection = ref('slide-left')
const debugMsg = ref('')
const scanHint = computed(() => {
  const config = store.cleanupConfig
  if (!config) return '加载全部照片'
  const orderMap = { oldest: '最旧优先', newest: '最新优先', random: '随机顺序' }
  const scope = config.scope === 'album' ? '指定相册' : '全部照片'
  return `${scope} · ${orderMap[config.sortOrder]} · ${config.batchSize} 张`
})
const currentDecision = computed(() => {
  if (!currentPhoto.value) return null
  return store.getDecision(currentPhoto.value.id)
})

onMounted(async () => {
  try {
    await loadAlbums()
    debugMsg.value = `config: ${JSON.stringify(store.cleanupConfig, null, 1)}\n`

    if (store.cleanupConfig) {
      await loadPhotosWithConfig(store.cleanupConfig)
    } else {
      await loadPhotos()
    }

    debugMsg.value += `loaded: ${photos.value.length} photos\n`
    if (photos.value.length > 0) {
      const p = photos.value[0]
      debugMsg.value += `first: id=${p.id}, uri=${p.uri?.substring(0, 50)}\n`
      debugMsg.value += `webPath: ${p.webPath?.substring(0, 60)}`
    }
    store.setPhotos(photos.value)
  } catch (e: any) {
    debugMsg.value = `ERROR: ${e?.message || e}\n${e?.stack || ''}`
  }
})

function goBack() {
  if (store.deleteCount > 0 || store.moveCount > 0) {
    router.push('/review')
  } else {
    router.push('/')
  }
}

function goToPhoto(index: number) {
  slideDirection.value = index > currentIndex.value ? 'slide-left' : 'slide-right'
  goToIndex(index)
}

function handleDelete() {
  if (!currentPhoto.value) return
  store.markDelete(currentPhoto.value)
  slideDirection.value = 'slide-left'
  nextPhoto()
}

async function handleMoveToAlbum(album: Album) {
  if (!currentPhoto.value) return
  try {
    await movePhotoToAlbum(currentPhoto.value.uri, album.name)
    store.markMove(currentPhoto.value, album)
    showAlbumSheet.value = false
    nextPhoto()
  } catch (e) {
    console.error('移动失败:', e)
  }
}

async function handleCreateAlbum(name: string) {
  const album = await createAlbum(name)
  if (album) {
    await handleMoveToAlbum(album)
  }
}

function handleUndo() {
  store.undoLast()
}

function handleMonthSelect(month: string) {
  store.selectedMonth = month
  showMonthPicker.value = false
}

function onTouchStart(e: TouchEvent) {
  touchStartX.value = e.touches[0].clientX
  touchStartY.value = e.touches[0].clientY
}

function onTouchEnd(e: TouchEvent) {
  const dx = e.changedTouches[0].clientX - touchStartX.value
  const dy = e.changedTouches[0].clientY - touchStartY.value
  // Only trigger if horizontal swipe > 50px and more horizontal than vertical
  if (Math.abs(dx) > 50 && Math.abs(dx) > Math.abs(dy)) {
    e.preventDefault()
    if (dx < 0) {
      slideDirection.value = 'slide-left'
      nextPhoto()
    } else {
      slideDirection.value = 'slide-right'
      prevPhoto()
    }
  }
}

function formatDate(ts: number): string {
  return new Date(ts).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}
</script>

<style scoped>
.cleanup-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
}

/* 调试浮层 */
.debug-toast {
  position: fixed;
  top: 60px;
  left: 8px;
  right: 8px;
  background: rgba(0,0,0,0.85);
  color: #0f0;
  padding: 10px;
  border-radius: 8px;
  font-size: 11px;
  z-index: 9999;
  max-height: 200px;
  overflow-y: auto;
}
.debug-toast pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

/* 扫描加载 */
.scan-overlay {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scan-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-xxl) var(--space-xl);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
}

.scan-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.scan-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.scan-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.photo-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: var(--space-sm);
  position: relative;
}

/* Slide transitions */
.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.25s ease, opacity 0.25s ease;
}

.slide-left-enter-from {
  transform: translateX(60px);
  opacity: 0;
}
.slide-left-leave-to {
  transform: translateX(-60px);
  opacity: 0;
}

.slide-right-enter-from {
  transform: translateX(-60px);
  opacity: 0;
}
.slide-right-leave-to {
  transform: translateX(60px);
  opacity: 0;
}

.photo-container {
  position: relative;
  max-width: 100%;
  max-height: 100%;
}

.main-photo {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
}

.decision-badge {
  position: absolute;
  top: var(--space-md);
  left: var(--space-md);
  padding: var(--space-xs) var(--space-md);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-semibold);
  backdrop-filter: var(--blur-sm);
  -webkit-backdrop-filter: var(--blur-sm);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.decision-badge.delete {
  background: rgba(var(--color-danger-rgb), 0.85);
}

.decision-badge.move {
  background: rgba(var(--color-primary-rgb), 0.85);
}

.empty-state {
  color: var(--color-text-secondary);
  font-size: var(--font-size-lg);
}

.detail-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  background: rgba(0, 0, 0, 0.95);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.detail-photo {
  max-width: 100%;
  max-height: 80%;
  object-fit: contain;
}

.detail-info {
  padding: var(--space-md);
  text-align: center;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.8;
}
</style>
