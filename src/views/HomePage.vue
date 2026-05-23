<template>
  <div class="home-page" :class="{ 'pixel-mode': isPixel }">
    <!-- 顶部区域 -->
    <div class="home-header">
      <div class="header-left">
        <h1 class="app-title">照片整理</h1>
        <p class="app-subtitle">智能管理你的照片库</p>
      </div>
      <button class="settings-btn" @click="$router.push('/settings')">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
          <circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 01-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 010-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 012.83-2.83l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 014 0v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/>
        </svg>
      </button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stat-card glass">
        <div class="stat-icon">🖼️</div>
        <span class="stat-number">{{ totalPhotos }}</span>
        <span class="stat-label">照片</span>
      </div>
      <div class="stat-card glass">
        <div class="stat-icon">📁</div>
        <span class="stat-number">{{ totalAlbums }}</span>
        <span class="stat-label">相册</span>
      </div>
      <div class="stat-card glass">
        <div class="stat-icon">💾</div>
        <span class="stat-number">{{ formattedSpace }}</span>
        <span class="stat-label">空间</span>
      </div>
    </div>

    <!-- 开始按钮区域 -->
    <div class="action-section">
      <div class="start-btn-glow"></div>
      <button class="start-btn" :disabled="loading" @click="startCleanup">
        <span v-if="loading" class="loading-spinner"></span>
        <template v-else>
          <svg class="start-icon" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M3 6l3 1m0 0l-3 9a5.002 5.002 0 006.001 0M6 7l3 9M6 7l6-2m6 2l3-1m-3 1l-3 9a5.002 5.002 0 006.001 0M18 7l3 9m-3-9l-6-2m0-2v2m0 16V5m0 16H9m3 0h3"/>
          </svg>
          <span class="start-text">开始整理</span>
        </template>
      </button>
      <p class="hint-text">扫描并整理手机中的所有照片</p>
    </div>

    <!-- 权限提示 -->
    <div v-if="!hasPermission" class="permission-notice glass">
      <div class="permission-icon">🔒</div>
      <p class="permission-text">需要存储权限才能读取照片</p>
      <button class="grant-btn" @click="requestPerms">授予权限</button>
    </div>

    <!-- 底部提示 -->
    <div class="footer-hint">
      <p>删除的照片将移入系统「最近删除」</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { usePermissions } from '@/composables/usePermissions'
import { usePhotos } from '@/composables/usePhotos'
import { useAlbums } from '@/composables/useAlbums'
import { useTheme } from '@/composables/useTheme'

const router = useRouter()
const { hasPermission, requestPermissions } = usePermissions()
const { photos, loading, loadPhotos } = usePhotos()
const { albums, loadAlbums } = useAlbums()
const { currentTheme } = useTheme()

const isPixel = computed(() => currentTheme.value === 'pixel')

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
  padding: var(--space-lg);
  padding-top: calc(var(--safe-area-top) + var(--space-lg));
  background: var(--color-bg);
  position: relative;
  overflow: hidden;
}

/* 背景装饰光晕 */
.home-page::before {
  content: '';
  position: absolute;
  top: -120px;
  right: -80px;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(var(--color-primary-rgb), 0.15) 0%, transparent 70%);
  pointer-events: none;
}

.home-page::after {
  content: '';
  position: absolute;
  bottom: -60px;
  left: -60px;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(var(--color-primary-rgb), 0.08) 0%, transparent 70%);
  pointer-events: none;
}

/* Header */
.home-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-xl);
  position: relative;
  z-index: 1;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.app-title {
  font-size: var(--font-size-hero);
  font-weight: var(--font-weight-bold);
  letter-spacing: -0.02em;
}

.app-subtitle {
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
}

.settings-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background: var(--color-surface);
  backdrop-filter: var(--blur-sm);
  -webkit-backdrop-filter: var(--blur-sm);
  color: var(--color-text-secondary);
  transition: background var(--transition-fast);
}

.settings-btn:active {
  background: var(--color-surface-2);
}

/* Stats */
.stats-section {
  display: flex;
  gap: var(--space-sm);
  margin-bottom: var(--space-xxl);
  position: relative;
  z-index: 1;
}

.stat-card {
  flex: 1;
  border-radius: var(--radius-lg);
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-xs);
  box-shadow: var(--shadow-md);
  border: 1px solid var(--color-border);
  transition: transform var(--transition-fast);
}

.stat-card:active {
  transform: scale(0.97);
}

.stat-icon {
  font-size: 24px;
  margin-bottom: var(--space-xs);
}

.stat-number {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
  line-height: 1;
}

.stat-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

/* Action */
.action-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-lg);
  position: relative;
  z-index: 1;
}

.start-btn-glow {
  position: absolute;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(var(--color-primary-rgb), 0.2) 0%, transparent 70%);
  animation: pulse-glow 3s ease-in-out infinite;
  pointer-events: none;
}

@keyframes pulse-glow {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.15); opacity: 1; }
}

.start-btn {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: linear-gradient(145deg, var(--color-primary), rgba(var(--color-primary-rgb), 0.7));
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: white;
  box-shadow: var(--shadow-glow), var(--shadow-lg);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
  position: relative;
  z-index: 2;
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.start-btn:active {
  transform: scale(0.93);
  box-shadow: 0 0 10px rgba(var(--color-primary-rgb), 0.2);
}

.start-btn:disabled {
  opacity: 0.6;
}

.start-icon {
  opacity: 0.9;
}

.start-text {
  font-size: var(--font-size-lg);
}

.hint-text {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-sm);
}

/* Permission */
.permission-notice {
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  text-align: center;
  margin-top: auto;
  margin-bottom: calc(var(--safe-area-bottom) + var(--space-md));
  border: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-sm);
  position: relative;
  z-index: 1;
}

.permission-icon {
  font-size: 28px;
}

.permission-text {
  color: var(--color-warning);
  font-size: var(--font-size-md);
}

.grant-btn {
  background: var(--color-primary);
  color: white;
  padding: var(--space-sm) var(--space-lg);
  border-radius: var(--radius-full);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  box-shadow: var(--shadow-sm);
}

/* Footer */
.footer-hint {
  text-align: center;
  padding: var(--space-md) 0;
  padding-bottom: calc(var(--safe-area-bottom) + var(--space-md));
  position: relative;
  z-index: 1;
}

.footer-hint p {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
}

/* Loading */
.loading-spinner {
  display: inline-block;
  width: 28px;
  height: 28px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Pixel theme overrides */
.pixel-mode .start-btn-glow {
  display: none;
}

.pixel-mode::before,
.pixel-mode::after {
  display: none;
}

.pixel-mode {
  background-color: var(--color-bg-secondary);
  background-image:
    linear-gradient(var(--color-border) 1px, transparent 1px),
    linear-gradient(90deg, var(--color-border) 1px, transparent 1px);
  background-size: 20px 20px;
}

.pixel-mode .start-btn {
  border-radius: var(--radius-lg);
  background: var(--color-primary);
  box-shadow: var(--shadow-lg);
  width: 140px;
  height: 140px;
  border: 3px solid #88aaff;
}

.pixel-mode .stat-card {
  box-shadow: var(--shadow-md);
}

.pixel-mode .app-title::after {
  content: '_';
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}
</style>
