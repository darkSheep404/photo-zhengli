<template>
  <div class="home-page" :class="{ 'pixel-mode': isPixel }">
    <!-- 顶部区域 -->
    <div class="home-header">
      <div class="header-left">
        <h1 class="app-title">📊 概览</h1>
        <p class="app-subtitle">照片整理助手</p>
      </div>
    </div>

    <!-- 累计统计 -->
    <div class="stats-section">
      <div class="stat-card glass">
        <div class="stat-icon">🗑</div>
        <span class="stat-number">{{ stats.totalDeleted }}</span>
        <span class="stat-label">已清理</span>
      </div>
      <div class="stat-card glass">
        <div class="stat-icon">📁</div>
        <span class="stat-number">{{ stats.totalMoved }}</span>
        <span class="stat-label">已归类</span>
      </div>
      <div class="stat-card glass">
        <div class="stat-icon">💾</div>
        <span class="stat-number">{{ formatBytes(stats.totalFreedBytes) }}</span>
        <span class="stat-label">已释放</span>
      </div>
    </div>

    <!-- 设备信息 -->
    <section class="device-section">
      <h3 class="section-label">设备相册</h3>
      <div class="device-info glass">
        <div class="device-row">
          <span> 相册数量</span>
          <span class="device-value">{{ totalAlbums }}</span>
        </div>
      </div>
    </section>

    <!-- 快捷操作 -->
    <section class="quick-section">
      <h3 class="section-label">快捷操作</h3>
      <div class="quick-actions">
        <button class="quick-btn" @click="$router.push('/cleanup')">
          🧹 开始整理
        </button>
        <button class="quick-btn" @click="$router.push('/albums')">
          📁 浏览相册
        </button>
      </div>
    </section>

    <!-- 最近记录 -->
    <section v-if="stats.records.length > 0" class="recent-section">
      <h3 class="section-label">最近清理</h3>
      <div class="recent-list">
        <div v-for="(record, i) in stats.records.slice(0, 3)" :key="i" class="recent-item">
          <span class="recent-date">{{ record.date }}</span>
          <span class="recent-detail">🗑{{ record.deletedCount }} 📁{{ record.movedCount }}</span>
        </div>
      </div>
    </section>

    <!-- 权限提示 -->
    <div v-if="!hasPermission" class="permission-notice glass">
      <div class="permission-icon">🔒</div>
      <p class="permission-text">需要存储权限才能读取照片</p>
      <button class="grant-btn" @click="requestPerms">授予权限</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { usePermissions } from '@/composables/usePermissions'
import { useAlbums } from '@/composables/useAlbums'
import { useTheme } from '@/composables/useTheme'
import { useStats } from '@/composables/useStats'

const { hasPermission, requestPermissions } = usePermissions()
const { albums, loadAlbums } = useAlbums()
const { currentTheme } = useTheme()
const { stats, formatBytes } = useStats()

const isPixel = computed(() => currentTheme.value === 'pixel')

const totalAlbums = ref(0)

onMounted(async () => {
  const granted = await requestPermissions()
  if (granted) {
    await loadAlbums()
    totalAlbums.value = albums.value.length
  }
})

async function requestPerms() {
  await requestPermissions()
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
  overflow-y: auto;
}

.home-header {
  margin-bottom: var(--space-xl);
}

.app-title {
  font-size: var(--font-size-xxl);
  font-weight: var(--font-weight-bold);
}

.app-subtitle {
  font-size: var(--font-size-md);
  color: var(--color-text-secondary);
  margin-top: var(--space-xs);
}

.stats-section {
  display: flex;
  gap: var(--space-sm);
  margin-bottom: var(--space-xl);
}

.stat-card {
  flex: 1;
  border-radius: var(--radius-lg);
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
}

.stat-icon { font-size: 20px; margin-bottom: var(--space-xs); }
.stat-number { font-size: var(--font-size-lg); font-weight: var(--font-weight-bold); color: var(--color-primary); line-height: 1; }
.stat-label { font-size: var(--font-size-xs); color: var(--color-text-secondary); }

.section-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-weight: var(--font-weight-medium);
  margin-bottom: var(--space-sm);
  padding-left: var(--space-xs);
}

.device-section { margin-bottom: var(--space-lg); }
.device-info { border-radius: var(--radius-lg); padding: var(--space-md); border: 1px solid var(--color-border); }
.device-row { display: flex; justify-content: space-between; padding: var(--space-sm) 0; font-size: var(--font-size-md); }
.device-row + .device-row { border-top: 1px solid var(--color-separator); }
.device-value { font-weight: var(--font-weight-semibold); color: var(--color-primary); }

.quick-section { margin-bottom: var(--space-lg); }
.quick-actions { display: flex; gap: var(--space-sm); }
.quick-btn {
  flex: 1; padding: var(--space-md); background: var(--color-primary); color: white;
  border-radius: var(--radius-md); font-size: var(--font-size-md); font-weight: var(--font-weight-semibold);
  box-shadow: var(--shadow-sm); transition: transform var(--transition-fast);
}
.quick-btn:active { transform: scale(0.97); }

.recent-section { margin-bottom: var(--space-lg); }
.recent-list { background: var(--color-surface); border-radius: var(--radius-md); border: 1px solid var(--color-border); overflow: hidden; }
.recent-item { display: flex; justify-content: space-between; padding: var(--space-sm) var(--space-md); font-size: var(--font-size-sm); }
.recent-item + .recent-item { border-top: 1px solid var(--color-separator); }
.recent-date { color: var(--color-text-secondary); }
.recent-detail { font-weight: var(--font-weight-medium); }

.permission-notice {
  border-radius: var(--radius-lg); padding: var(--space-lg); text-align: center;
  border: 1px solid var(--color-border); display: flex; flex-direction: column; align-items: center; gap: var(--space-sm);
}
.permission-icon { font-size: 28px; }
.permission-text { color: var(--color-warning); font-size: var(--font-size-md); }
.grant-btn {
  background: var(--color-primary); color: white; padding: var(--space-sm) var(--space-lg);
  border-radius: var(--radius-full); font-size: var(--font-size-md); font-weight: var(--font-weight-semibold);
}

.pixel-mode {
  background-color: var(--color-bg-secondary);
  background-image: linear-gradient(var(--color-border) 1px, transparent 1px), linear-gradient(90deg, var(--color-border) 1px, transparent 1px);
  background-size: 20px 20px;
}
.pixel-mode .app-title::after { content: '_'; animation: blink 1s step-end infinite; }
@keyframes blink { 0%, 50% { opacity: 1; } 51%, 100% { opacity: 0; } }
</style>
