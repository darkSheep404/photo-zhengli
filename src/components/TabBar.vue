<template>
  <nav class="tab-bar">
    <router-link
      v-for="tab in tabs"
      :key="tab.path"
      :to="tab.path"
      class="tab-item"
      active-class="active"
    >
      <span class="tab-icon">{{ tab.icon }}</span>
      <span class="tab-label">{{ tab.label }}</span>
    </router-link>
  </nav>
</template>

<script setup lang="ts">
const tabs = [
  { path: '/', icon: '📊', label: '概览' },
  { path: '/cleanup', icon: '🧹', label: '整理' },
  { path: '/albums', icon: '📁', label: '相册' },
  { path: '/stats', icon: '📈', label: '统计' },
  { path: '/settings', icon: '⚙️', label: '设置' },
]
</script>

<style scoped>
.tab-bar {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: var(--space-xs) 0;
  padding-bottom: calc(var(--safe-area-bottom) + var(--space-xs));
  background: var(--color-surface-solid);
  border-top: 1px solid var(--color-separator);
  position: relative;
  z-index: 20;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: var(--space-xs) var(--space-sm);
  text-decoration: none;
  color: var(--color-text-secondary);
  transition: color var(--transition-fast);
  min-width: 56px;
}

.tab-item.active {
  color: var(--color-primary);
}

.tab-icon {
  font-size: 20px;
  line-height: 1;
}

.tab-label {
  font-size: var(--font-size-xs);
  font-weight: var(--font-weight-medium);
}
</style>
