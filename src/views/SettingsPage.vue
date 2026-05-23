<template>
  <div class="settings-page">
    <div class="settings-header">
      <button @click="$router.back()">← 返回</button>
      <h2>设置</h2>
      <span></span>
    </div>

    <div class="settings-list">
      <div class="setting-item">
        <span>排序方式</span>
        <select v-model="sortBy">
          <option value="date-desc">日期（最新优先）</option>
          <option value="date-asc">日期（最早优先）</option>
          <option value="size-desc">大小（最大优先）</option>
          <option value="size-asc">大小（最小优先）</option>
        </select>
      </div>

      <div class="setting-item">
        <span>关于</span>
        <span class="setting-value">照片整理 v0.1.0</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const sortBy = ref('date-desc')
</script>

<style scoped>
.settings-page {
  height: 100%;
  background: var(--color-bg);
  padding-top: var(--safe-area-top);
}

.settings-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border);
}

.settings-header h2 {
  font-size: 17px;
}

.settings-header button {
  color: var(--color-primary);
  font-size: 14px;
}

.settings-list {
  padding: 16px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border);
  font-size: 15px;
}

.setting-value {
  color: var(--color-text-secondary);
  font-size: 14px;
}

select {
  background: var(--color-surface);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 14px;
}
</style>
