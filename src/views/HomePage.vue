<template>
  <div class="home-page">
    <div class="home-header">
      <h1 class="app-title">📷 照片整理</h1>
      <button class="settings-btn" @click="$router.push('/settings')">
        ⚙️
      </button>
    </div>

    <div class="stats-section">
      <div class="stat-card">
        <span class="stat-number">{{ totalPhotos }}</span>
        <span class="stat-label">总照片数</span>
      </div>
      <div class="stat-card">
        <span class="stat-number">{{ totalAlbums }}</span>
        <span class="stat-label">相册数</span>
      </div>
      <div class="stat-card">
        <span class="stat-number">{{ formattedSpace }}</span>
        <span class="stat-label">占用空间</span>
      </div>
    </div>

    <div class="action-section">
      <button class="start-btn" :disabled="loading" @click="startCleanup">
        <span v-if="loading" class="loading-spinner"></span>
        <span v-else>开始整理</span>
      </button>
      <p class="hint-text">扫描手机照片，快速整理删除和归类</p>
    </div>

    <div v-if="!hasPermission" class="permission-notice">
      <p>需要存储权限才能读取照片</p>
      <button class="grant-btn" @click="requestPerms">授予权限</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { usePermissions } from '@/composables/usePermissions'
import { usePhotos } from '@/composables/usePhotos'
import { useAlbums } from '@/composables/useAlbums'

const router = useRouter()
const { hasPermission, requestPermissions } = usePermissions()
const { photos, loading, loadPhotos } = usePhotos()
const { albums, loadAlbums } = useAlbums()

const totalPhotos = ref(0)
const totalAlbums = ref(0)
const formattedSpace = ref('--')

onMounted(async () => {
  const granted = await requestPermissions()
  if (granted) {
    await Promise.all([loadPhotos(1000), loadAlbums()])
    totalPhotos.value = photos.value.length
    totalAlbums.value = albums.value.length
    const totalBytes = photos.value.reduce((sum, p) => sum + p.size, 0)
    formattedSpace.value = formatBytes(totalBytes)
  }
})

async function requestPerms() {
  await requestPermissions()
}

function startCleanup() {
  router.push('/cleanup')
}

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}
</script>

<style scoped>
.home-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
  padding-top: calc(var(--safe-area-top) + 20px);
  background: linear-gradient(180deg, #1a1a2e 0%, #0a0a0a 100%);
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.app-title {
  font-size: 28px;
  font-weight: 700;
}

.settings-btn {
  font-size: 24px;
  padding: 8px;
}

.stats-section {
  display: flex;
  gap: 12px;
  margin-bottom: 48px;
}

.stat-card {
  flex: 1;
  background: var(--color-surface);
  border-radius: 16px;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-primary);
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.action-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.start-btn {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), #2d6bb5);
  font-size: 22px;
  font-weight: 600;
  color: white;
  box-shadow: 0 8px 32px rgba(74, 144, 217, 0.3);
  transition: transform 0.2s, box-shadow 0.2s;
}

.start-btn:active {
  transform: scale(0.95);
  box-shadow: 0 4px 16px rgba(74, 144, 217, 0.2);
}

.start-btn:disabled {
  opacity: 0.6;
}

.hint-text {
  color: var(--color-text-secondary);
  font-size: 14px;
}

.permission-notice {
  background: var(--color-surface);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  margin-top: auto;
  margin-bottom: calc(var(--safe-area-bottom) + 16px);
}

.permission-notice p {
  margin-bottom: 12px;
  color: var(--color-warning);
}

.grant-btn {
  background: var(--color-primary);
  color: white;
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
}

.loading-spinner {
  display: inline-block;
  width: 24px;
  height: 24px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
