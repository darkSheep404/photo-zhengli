<template>
  <div class="cleanup-page">
    <!-- 顶部信息栏 -->
    <ProgressBar
      :current="currentIndex + 1"
      :total="photos.length"
      :delete-count="store.deleteCount"
      :selected-month="store.selectedMonth"
      @back="goBack"
      @month-click="showMonthPicker = true"
    />

    <!-- 中间大图区域 -->
    <div class="photo-area" @click="showDetail = true">
      <div v-if="currentPhoto" class="photo-container">
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
      <div v-else class="empty-state">
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
const { photos, currentIndex, currentPhoto, loadPhotos, goToIndex, nextPhoto } = usePhotos()
const { albums, loadAlbums, createAlbum } = useAlbums()

const showAlbumSheet = ref(false)
const showMonthPicker = ref(false)
const showDetail = ref(false)

const currentDecision = computed(() => {
  if (!currentPhoto.value) return null
  return store.getDecision(currentPhoto.value.id)
})

onMounted(async () => {
  await Promise.all([loadPhotos(), loadAlbums()])
  store.setPhotos(photos.value)
})

function goBack() {
  if (store.deleteCount > 0 || store.moveCount > 0) {
    router.push('/review')
  } else {
    router.push('/')
  }
}

function goToPhoto(index: number) {
  goToIndex(index)
}

function handleDelete() {
  if (!currentPhoto.value) return
  store.markDelete(currentPhoto.value)
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

.photo-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  padding: var(--space-sm);
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
