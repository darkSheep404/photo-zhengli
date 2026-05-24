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

    <!-- 浮层提示 -->
    <div v-if="toastMsg" class="toast-msg" @click="toastMsg = ''">
      {{ toastMsg }}
    </div>

    <!-- 顶部信息栏 -->
    <ProgressBar
      v-if="!loading"
      :current="currentIndex + 1"
      :total="photos.length"
      :delete-count="store.deleteCount"
      :has-decisions="store.decisions.length > 0"
      :selected-month="store.selectedMonth"
      :photo-time="currentPhoto?.createdAt ?? 0"
      @back="goBack"
      @month-click="showMonthPicker = true"
      @review="goReview"
    />

    <!-- 中间大图区域 -->
    <div v-if="!loading" class="photo-area"
      @touchstart="onTouchStart"
      @touchend="onTouchEnd"
      @click="openDetail"
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
            {{ currentDecision.action === 'delete' ? '待删除' : currentDecision.action === 'keep' ? '保留' : `已分类 · ${currentDecision.targetAlbum?.name}` }}
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
      <div class="detail-info-card" @click.stop>
        <div class="detail-row detail-filename">
          <span class="detail-icon">📷</span>
          <span class="detail-text">{{ currentPhoto.filename }}</span>
        </div>
        <div class="detail-meta">
          <div class="detail-row">
            <span class="detail-icon">📅</span>
            <span class="detail-text">{{ formatDate(currentPhoto.createdAt) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-icon">📐</span>
            <span class="detail-text">{{ currentPhoto.width }} × {{ currentPhoto.height }}</span>
          </div>
        </div>
        <div class="detail-meta">
          <div class="detail-row">
            <span class="detail-icon">💾</span>
            <span class="detail-text">{{ formatBytes(currentPhoto.size) }}</span>
          </div>
          <div v-if="currentPhoto.albumName" class="detail-row">
            <span class="detail-icon">📁</span>
            <span class="detail-text">{{ currentPhoto.albumName }}</span>
          </div>
        </div>
        <div v-if="exifInfo" class="detail-meta">
          <div v-if="exifInfo.location" class="detail-row">
            <span class="detail-icon">📍</span>
            <span class="detail-text">{{ exifInfo.location }}</span>
          </div>
          <div v-if="exifInfo.camera" class="detail-row">
            <span class="detail-icon">📸</span>
            <span class="detail-text">{{ exifInfo.camera }}</span>
          </div>
        </div>
        <div v-if="exifLoading" class="detail-meta">
          <div class="detail-row">
            <span class="detail-icon">⏳</span>
            <span class="detail-text">读取EXIF...</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 退出确认对话框 -->
    <div v-if="showQuitConfirm" class="quit-overlay" @click="showQuitConfirm = false">
      <div class="quit-dialog" @click.stop>
        <h3>放弃本次整理？</h3>
        <p>已标记的 {{ store.decisions.length }} 条操作将被丢弃</p>
        <div class="quit-actions">
          <button class="quit-btn cancel" @click="showQuitConfirm = false">继续整理</button>
          <button class="quit-btn confirm" @click="confirmQuit">放弃并返回</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { usePhotos } from '@/composables/usePhotos'
import { useAlbums } from '@/composables/useAlbums'
import { useCleanupStore } from '@/store/cleanupStore'
import { movePhotoToAlbum } from '@/composables/useMovePhoto'
import { Capacitor } from '@capacitor/core'
import { MediaAccessPlugin } from '@/plugins/mediaAccess'
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
const exifInfo = ref<{ location: string; camera: string } | null>(null)
const exifLoading = ref(false)
const exifCache = new Map<string, { location: string; camera: string } | null>()
const touchStartX = ref(0)
const touchStartY = ref(0)
const slideDirection = ref('slide-left')
const toastMsg = ref('')
const scanHint = computed(() => {
  const config = store.cleanupConfig
  if (!config) return '加载全部照片'
  const orderMap = { oldest: '最旧优先', newest: '最新优先', random: '随机顺序' }
  const scope = config.scope === 'album' ? '指定相册' : config.scope === 'reviewed' ? '已保留照片' : '全部照片'
  return `${scope} · ${orderMap[config.sortOrder]} · ${config.batchSize} 张`
})
const currentDecision = computed(() => {
  if (!currentPhoto.value) return null
  return store.getDecision(currentPhoto.value.id)
})

onMounted(async () => {
  try {
    await loadAlbums()

    if (store.cleanupConfig) {
      await loadPhotosWithConfig(store.cleanupConfig)
    } else {
      await loadPhotos()
    }

    store.setPhotos(photos.value)
  } catch (e: any) {
    toastMsg.value = `加载失败: ${e?.message || e}`
  }
})

const showQuitConfirm = ref(false)

function goBack() {
  if (store.decisions.length > 0) {
    showQuitConfirm.value = true
  } else {
    router.push('/')
  }
}

function confirmQuit() {
  store.clearAll()
  router.push('/')
}

function goReview() {
  if (store.decisions.length > 0) {
    router.push('/review')
  }
}

function goToPhoto(index: number) {
  // 跳转时，当前照片（如果向前跳）标记为保留
  if (index > currentIndex.value && currentPhoto.value) {
    store.markKeep(currentPhoto.value)
  }
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
  } catch (e: any) {
    console.error('移动失败:', e)
    toastMsg.value = `移动失败: ${e?.message || e}`
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
      // 左滑下一张：自动标记当前照片为保留（若未标记）
      if (currentPhoto.value) {
        store.markKeep(currentPhoto.value)
      }
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

async function openDetail() {
  showDetail.value = true
  if (!currentPhoto.value) return
  const photo = currentPhoto.value
  const cached = exifCache.get(photo.id)
  if (cached !== undefined) {
    exifInfo.value = cached
    return
  }
  if (!Capacitor.isNativePlatform()) {
    exifInfo.value = null
    return
  }
  exifLoading.value = true
  exifInfo.value = null
  try {
    const result = await MediaAccessPlugin.getPhotoExif({ contentUri: photo.uri })
    let location = ''
    if (result.latitude != null && result.longitude != null) {
      location = await reverseGeocode(result.latitude, result.longitude)
    }
    let camera = ''
    if (result.make || result.model) {
      camera = [result.make, result.model].filter(Boolean).join(' ')
    }
    const info = (location || camera) ? { location, camera } : null
    exifCache.set(photo.id, info)
    if (currentPhoto.value?.id === photo.id) {
      exifInfo.value = info
    }
  } catch {
    exifCache.set(photo.id, null)
    exifInfo.value = null
  } finally {
    exifLoading.value = false
  }
}

async function reverseGeocode(lat: number, lng: number): Promise<string> {
  try {
    const res = await fetch(
      `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&zoom=10&accept-language=zh`,
      { headers: { 'User-Agent': 'PhotoZhengli/1.0' } }
    )
    const data = await res.json()
    const addr = data.address
    if (!addr) return `${lat.toFixed(4)}, ${lng.toFixed(4)}`
    // 优先显示城市级别
    return addr.city || addr.town || addr.county || addr.state || addr.country || `${lat.toFixed(4)}, ${lng.toFixed(4)}`
  } catch {
    return `${lat.toFixed(4)}, ${lng.toFixed(4)}`
  }
}
</script>

<style scoped>
.cleanup-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--color-bg);
}

/* 浮层提示 */
.toast-msg {
  position: fixed;
  top: 70px;
  left: 16px;
  right: 16px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  z-index: 9999;
  text-align: center;
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

.decision-badge.keep {
  background: rgba(var(--color-success-rgb, 52, 199, 89), 0.85);
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
  max-height: 70%;
  object-fit: contain;
}

.detail-info-card {
  width: 90%;
  max-width: 360px;
  margin-top: var(--space-md);
  background: var(--color-surface);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  border-radius: var(--radius-xl, 16px);
  border: 1px solid var(--color-border);
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.detail-filename {
  padding-bottom: var(--space-xs);
  border-bottom: 1px solid var(--color-separator);
}

.detail-filename .detail-text {
  font-weight: var(--font-weight-semibold);
  color: var(--color-text);
  word-break: break-all;
}

.detail-meta {
  display: flex;
  gap: var(--space-md);
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
}

.detail-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.detail-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 退出确认对话框 */
.quit-overlay {
  position: fixed;
  inset: 0;
  z-index: 200;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.quit-dialog {
  background: var(--color-surface-solid);
  border-radius: var(--radius-xl);
  padding: var(--space-xl);
  text-align: center;
  max-width: 280px;
  width: 80%;
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--color-border);
}

.quit-dialog h3 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  margin-bottom: var(--space-sm);
}

.quit-dialog p {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-lg);
}

.quit-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.quit-btn {
  width: 100%;
  padding: var(--space-md);
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  transition: transform var(--transition-fast);
}

.quit-btn:active {
  transform: scale(0.98);
}

.quit-btn.cancel {
  background: var(--color-primary);
  color: white;
}

.quit-btn.confirm {
  background: var(--color-surface-2);
  color: var(--color-danger);
}
</style>
