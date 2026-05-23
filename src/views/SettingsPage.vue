<template>
  <div class="settings-page">
    <div class="settings-header">
      <button @click="$router.back()">← 返回</button>
      <h2>设置</h2>
      <span></span>
    </div>

    <div class="settings-list">
      <!-- 主题选择 -->
      <div class="setting-section">
        <h3 class="section-label">外观主题</h3>
        <div class="theme-picker">
          <button
            class="theme-option"
            :class="{ active: currentTheme === 'ios' }"
            @click="setTheme('ios')"
          >
            <div class="theme-preview ios-preview">
              <div class="preview-dot"></div>
              <div class="preview-line"></div>
              <div class="preview-line short"></div>
            </div>
            <span class="theme-name">iOS 风格</span>
            <span class="theme-desc">磨砂玻璃 · 圆角</span>
          </button>
          <button
            class="theme-option"
            :class="{ active: currentTheme === 'pixel' }"
            @click="setTheme('pixel')"
          >
            <div class="theme-preview pixel-preview">
              <div class="preview-block"></div>
              <div class="preview-line"></div>
              <div class="preview-line short"></div>
            </div>
            <span class="theme-name">像素风</span>
            <span class="theme-desc">复古 · 8-bit</span>
          </button>
        </div>
      </div>

      <!-- 排序设置 -->
      <div class="setting-section">
        <h3 class="section-label">照片排序</h3>
        <div class="setting-item">
          <span>排序方式</span>
          <select v-model="sortBy">
            <option value="date-desc">日期（最新优先）</option>
            <option value="date-asc">日期（最早优先）</option>
            <option value="size-desc">大小（最大优先）</option>
            <option value="size-asc">大小（最小优先）</option>
          </select>
        </div>
      </div>

      <!-- 关于 -->
      <div class="setting-section">
        <h3 class="section-label">关于</h3>
        <div class="setting-item">
          <span>版本</span>
          <span class="setting-value">照片整理 v0.1.0</span>
        </div>
        <div class="setting-item">
          <span>删除机制</span>
          <span class="setting-value">移入最近删除</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useTheme } from '@/composables/useTheme'

const { currentTheme, setTheme } = useTheme()
const sortBy = ref('date-desc')
</script>

<style scoped>
.settings-page {
  height: 100%;
  background: var(--color-bg);
  padding-top: var(--safe-area-top);
  overflow-y: auto;
}

.settings-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-md);
  border-bottom: 1px solid var(--color-separator);
  background: var(--color-surface);
  backdrop-filter: var(--blur-md);
  -webkit-backdrop-filter: var(--blur-md);
  position: sticky;
  top: 0;
  z-index: 10;
}

.settings-header h2 {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
}

.settings-header button {
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.settings-list {
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  gap: var(--space-lg);
}

.setting-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.section-label {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-weight: var(--font-weight-medium);
  padding-left: var(--space-xs);
}

/* Theme Picker */
.theme-picker {
  display: flex;
  gap: var(--space-sm);
}

.theme-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-md);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 2px solid var(--color-border);
  transition: all var(--transition-fast);
}

.theme-option.active {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-glow);
}

.theme-option:active {
  transform: scale(0.97);
}

.theme-preview {
  width: 60px;
  height: 60px;
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px;
}

.ios-preview {
  background: linear-gradient(135deg, #1c1c1e, #2c2c2e);
  border-radius: 14px;
}

.ios-preview .preview-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #007AFF;
}

.ios-preview .preview-line {
  width: 80%;
  height: 4px;
  border-radius: 2px;
  background: rgba(255, 255, 255, 0.2);
}

.ios-preview .preview-line.short {
  width: 50%;
}

.pixel-preview {
  background: linear-gradient(135deg, #1a1a2e, #0f3460);
  border-radius: 4px;
  border: 2px solid #333366;
}

.pixel-preview .preview-block {
  width: 12px;
  height: 12px;
  background: #5B8CFF;
}

.pixel-preview .preview-line {
  width: 80%;
  height: 3px;
  background: rgba(224, 224, 255, 0.3);
}

.pixel-preview .preview-line.short {
  width: 50%;
}

.theme-name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
}

.theme-desc {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}

/* Setting Items */
.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-md);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  border: 1px solid var(--color-border);
}

.setting-item + .setting-item {
  margin-top: 1px;
}

.setting-value {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

select {
  background: var(--color-surface-2);
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  padding: var(--space-xs) var(--space-md);
  font-size: var(--font-size-sm);
  font-family: var(--font-family);
}
</style>
