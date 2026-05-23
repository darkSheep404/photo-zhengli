<template>
  <div class="config-page">
    <div class="config-header">
      <h1>🧹 开始整理</h1>
      <p class="config-subtitle">选择清理范围和方式</p>
    </div>

    <!-- 范围选择 -->
    <section class="config-section">
      <h3 class="section-label">清理范围</h3>
      <div class="option-group">
        <button
          class="option-card"
          :class="{ active: scope === 'all' }"
          @click="scope = 'all'"
        >
          <span class="option-icon">📷</span>
          <div class="option-info">
            <span class="option-title">全部照片</span>
            <span class="option-desc">扫描所有相册</span>
          </div>
          <span v-if="scope === 'all'" class="check">✓</span>
        </button>
        <button
          class="option-card"
          :class="{ active: scope === 'album' }"
          @click="scope = 'album'"
        >
          <span class="option-icon">📁</span>
          <div class="option-info">
            <span class="option-title">指定相册</span>
            <span class="option-desc">选择要整理的相册</span>
          </div>
          <span v-if="scope === 'album'" class="check">✓</span>
        </button>
      </div>
    </section>

    <!-- 相册选择 (仅当 scope === 'album' 时) -->
    <section v-if="scope === 'album'" class="config-section">
      <h3 class="section-label">选择相册</h3>
      <div class="album-select-list">
        <label
          v-for="album in albums"
          :key="album.id"
          class="album-checkbox"
          :class="{ checked: selectedAlbums.has(album.id) }"
        >
          <input
            type="checkbox"
            :checked="selectedAlbums.has(album.id)"
            @change="toggleAlbum(album.id)"
          />
          <span class="album-name">{{ album.name }}</span>
          <span class="album-count">{{ album.count }} 张</span>
        </label>
      </div>
    </section>

    <!-- 排序方式 -->
    <section class="config-section">
      <h3 class="section-label">清理顺序</h3>
      <div class="sort-options">
        <button
          v-for="opt in sortOptions"
          :key="opt.value"
          class="sort-chip"
          :class="{ active: sortOrder === opt.value }"
          @click="sortOrder = opt.value"
        >
          {{ opt.icon }} {{ opt.label }}
        </button>
      </div>
    </section>

    <!-- 加载数量 -->
    <section class="config-section">
      <h3 class="section-label">每次加载数量</h3>
      <div class="quantity-options">
        <button
          v-for="qty in quantityOptions"
          :key="qty"
          class="qty-chip"
          :class="{ active: batchSize === qty }"
          @click="batchSize = qty"
        >
          {{ qty }} 张
        </button>
      </div>
    </section>

    <!-- 开始按钮 -->
    <div class="start-section">
      <button
        class="start-cleanup-btn"
        :disabled="scope === 'album' && selectedAlbums.size === 0"
        @click="startCleanup"
      >
        ▶ 开始清理
        <span v-if="scope === 'album' && selectedAlbums.size > 0" class="start-info">
          ({{ selectedAlbumsCount }} 张照片)
        </span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAlbums } from '@/composables/useAlbums'
import { useCleanupStore } from '@/store/cleanupStore'

const router = useRouter()
const { albums, loadAlbums } = useAlbums()
const store = useCleanupStore()

const scope = ref<'all' | 'album'>('all')
const selectedAlbums = ref(new Set<string>())
const sortOrder = ref<'oldest' | 'newest' | 'random'>('oldest')
const batchSize = ref(50)

const sortOptions = [
  { value: 'oldest' as const, label: '最旧优先', icon: '📆' },
  { value: 'newest' as const, label: '最新优先', icon: '🆕' },
  { value: 'random' as const, label: '随机', icon: '🎲' },
]

const quantityOptions = [20, 50, 100, 200]

const selectedAlbumsCount = computed(() => {
  return albums.value
    .filter(a => selectedAlbums.value.has(a.id))
    .reduce((sum, a) => sum + a.count, 0)
})

onMounted(async () => {
  await loadAlbums()
})

function toggleAlbum(id: string) {
  if (selectedAlbums.value.has(id)) {
    selectedAlbums.value.delete(id)
  } else {
    selectedAlbums.value.add(id)
  }
}

function startCleanup() {
  store.cleanupConfig = {
    scope: scope.value,
    albumIds: scope.value === 'album' ? [...selectedAlbums.value] : [],
    sortOrder: sortOrder.value,
    batchSize: batchSize.value,
  }
  router.push('/cleanup/session')
}
</script>

<style scoped>
.config-page {
  height: 100%;
  overflow-y: auto;
  padding: var(--space-lg);
  padding-top: calc(var(--safe-area-top) + var(--space-lg));
  background: var(--color-bg);
}

.config-header {
  margin-bottom: var(--space-xl);
}

.config-header h1 {
  font-size: var(--font-size-xxl);
  font-weight: var(--font-weight-bold);
  margin-bottom: var(--space-xs);
}

.config-subtitle {
  color: var(--color-text-secondary);
  font-size: var(--font-size-md);
}

.config-section {
  margin-bottom: var(--space-lg);
}

.section-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-weight: var(--font-weight-medium);
  margin-bottom: var(--space-sm);
  padding-left: var(--space-xs);
}

/* Scope options */
.option-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.option-card {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  border: 2px solid var(--color-border);
  transition: all var(--transition-fast);
  text-align: left;
}

.option-card.active {
  border-color: var(--color-primary);
  background: rgba(var(--color-primary-rgb), 0.08);
}

.option-card:active {
  transform: scale(0.98);
}

.option-icon {
  font-size: 28px;
}

.option-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.option-title {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
}

.option-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.check {
  color: var(--color-primary);
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
}

/* Album selection */
.album-select-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
  max-height: 200px;
  overflow-y: auto;
  background: var(--color-surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  padding: var(--space-sm);
}

.album-checkbox {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.album-checkbox:active {
  background: var(--color-surface-2);
}

.album-checkbox.checked {
  background: rgba(var(--color-primary-rgb), 0.1);
}

.album-checkbox input[type="checkbox"] {
  width: 20px;
  height: 20px;
  accent-color: var(--color-primary);
}

.album-checkbox .album-name {
  flex: 1;
  font-size: var(--font-size-md);
}

.album-checkbox .album-count {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

/* Sort options */
.sort-options {
  display: flex;
  gap: var(--space-sm);
  flex-wrap: wrap;
}

.sort-chip {
  padding: var(--space-sm) var(--space-md);
  background: var(--color-surface);
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  border: 1px solid var(--color-border);
  transition: all var(--transition-fast);
}

.sort-chip.active {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

.sort-chip:active {
  transform: scale(0.95);
}

/* Quantity options */
.quantity-options {
  display: flex;
  gap: var(--space-sm);
}

.qty-chip {
  flex: 1;
  padding: var(--space-sm) var(--space-md);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  border: 1px solid var(--color-border);
  text-align: center;
  transition: all var(--transition-fast);
}

.qty-chip.active {
  background: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}

/* Start button */
.start-section {
  padding: var(--space-lg) 0;
  padding-bottom: var(--space-xl);
}

.start-cleanup-btn {
  width: 100%;
  padding: var(--space-md);
  background: var(--color-primary);
  color: white;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-glow);
  transition: transform var(--transition-fast);
}

.start-cleanup-btn:active {
  transform: scale(0.98);
}

.start-cleanup-btn:disabled {
  opacity: 0.5;
  box-shadow: none;
}

.start-info {
  font-size: var(--font-size-sm);
  opacity: 0.8;
}
</style>
