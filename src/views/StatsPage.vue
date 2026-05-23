<template>
  <div class="stats-page">
    <div class="stats-header">
      <h1>📈 清理统计</h1>
    </div>

    <!-- 总计卡片 -->
    <div class="summary-grid">
      <div class="summary-card glass">
        <span class="summary-number">{{ stats.totalDeleted }}</span>
        <span class="summary-label">已清理</span>
        <span class="summary-unit">张照片</span>
      </div>
      <div class="summary-card glass">
        <span class="summary-number">{{ formatBytes(stats.totalFreedBytes) }}</span>
        <span class="summary-label">已释放</span>
        <span class="summary-unit">存储空间</span>
      </div>
      <div class="summary-card glass">
        <span class="summary-number">{{ stats.totalMoved }}</span>
        <span class="summary-label">已分类</span>
        <span class="summary-unit">张照片</span>
      </div>
      <div class="summary-card glass">
        <span class="summary-number">{{ stats.totalKept || 0 }}</span>
        <span class="summary-label">已保留</span>
        <span class="summary-unit">张照片</span>
      </div>
      <div class="summary-card glass">
        <span class="summary-number">{{ formatDuration(stats.totalDurationMs) }}</span>
        <span class="summary-label">整理时长</span>
        <span class="summary-unit">累计</span>
      </div>
    </div>

    <!-- 历史记录 -->
    <section class="records-section">
      <h3 class="section-label">最近记录</h3>
      <div v-if="stats.records.length === 0" class="empty-records">
        <p>还没有清理记录</p>
        <p class="empty-hint">去「整理」tab 开始整理照片吧</p>
      </div>
      <div v-else class="record-list">
        <div v-for="(record, i) in stats.records" :key="i" class="record-item">
          <div class="record-date">
            <span class="date-icon">📅</span>
            <span>{{ record.date }}</span>
            <span v-if="record.durationMs" class="record-duration">⏱ {{ formatDuration(record.durationMs) }}</span>
          </div>
          <div class="record-details">
            <span v-if="record.deletedCount" class="detail-chip danger">
              🗑 {{ record.deletedCount }} 张
            </span>
            <span v-if="record.movedCount" class="detail-chip primary">
              📁 {{ record.movedCount }} 张
            </span>
            <span v-if="record.keptCount" class="detail-chip success">
              ✓ 保留 {{ record.keptCount }} 张
            </span>
            <span class="detail-chip">
              💾 {{ formatBytes(record.freedBytes) }}
            </span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useStats } from '@/composables/useStats'

const { stats, formatDuration, formatBytes } = useStats()
</script>

<style scoped>
.stats-page {
  height: 100%;
  overflow-y: auto;
  padding: var(--space-lg);
  padding-top: calc(var(--safe-area-top) + var(--space-lg));
  background: var(--color-bg);
}

.stats-header {
  margin-bottom: var(--space-xl);
}

.stats-header h1 {
  font-size: var(--font-size-xxl);
  font-weight: var(--font-weight-bold);
}

.summary-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-sm);
  margin-bottom: var(--space-xl);
}

.summary-card {
  border-radius: var(--radius-lg);
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.summary-number {
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
  line-height: 1.2;
}

.summary-label {
  font-size: var(--font-size-sm);
  color: var(--color-text);
  font-weight: var(--font-weight-medium);
}

.summary-unit {
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
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

.records-section {
  margin-bottom: var(--space-lg);
}

.empty-records {
  text-align: center;
  padding: var(--space-xxl) 0;
  color: var(--color-text-secondary);
}

.empty-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-tertiary);
  margin-top: var(--space-sm);
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.record-item {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-md);
  border: 1px solid var(--color-border);
}

.record-date {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  margin-bottom: var(--space-sm);
}

.date-icon {
  font-size: 14px;
}

.record-duration {
  margin-left: auto;
  font-size: var(--font-size-xs);
  color: var(--color-text-tertiary);
  font-weight: var(--font-weight-regular);
}

.record-details {
  display: flex;
  gap: var(--space-sm);
  flex-wrap: wrap;
}

.detail-chip {
  font-size: var(--font-size-xs);
  padding: 2px var(--space-sm);
  border-radius: var(--radius-full);
  background: var(--color-surface-2);
}

.detail-chip.danger {
  background: rgba(var(--color-danger-rgb), 0.15);
  color: var(--color-danger);
}

.detail-chip.primary {
  background: rgba(var(--color-primary-rgb), 0.15);
  color: var(--color-primary);
}

.detail-chip.success {
  background: rgba(52, 199, 89, 0.15);
  color: #34C759;
}
</style>
