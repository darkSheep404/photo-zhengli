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

      <!-- 数据管理 -->
      <div class="setting-section">
        <h3 class="section-label">数据管理</h3>
        <div class="setting-item clickable" @click="handleClearReviewed">
          <span>清空已保留照片记录</span>
          <span class="setting-value">{{ reviewedCount }} 张</span>
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
        <div class="setting-item clickable" @click="showQrCode = true">
          <span>联系我们 / 反馈问题</span>
          <span class="setting-value">微信 ›</span>
        </div>
      </div>
    </div>

    <!-- 微信二维码弹窗 -->
    <div v-if="showQrCode" class="qr-overlay" @click="showQrCode = false">
      <div class="qr-dialog" @click.stop>
        <span class="qr-close" @click="showQrCode = false">✕</span>
        <h3 class="qr-title">联系我们</h3>
        <p class="qr-hint">扫描下方二维码添加微信</p>
        <img src="/img/wechat-qr.png" alt="微信二维码" class="qr-image" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useTheme } from '@/composables/useTheme'
import { useReviewedPhotos } from '@/composables/useReviewedPhotos'

const { currentTheme, setTheme } = useTheme()
const { getReviewedCount, clearReviewed } = useReviewedPhotos()
const sortBy = ref('date-desc')
const reviewedCount = ref(getReviewedCount())
const showQrCode = ref(false)

function handleClearReviewed() {
  if (reviewedCount.value === 0) return
  if (confirm(`确定清空 ${reviewedCount.value} 张已保留照片的记录？\n清空后这些照片将重新出现在清理列表中。`)) {
    clearReviewed()
    reviewedCount.value = 0
  }
}
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

.setting-item.clickable {
  cursor: pointer;
  transition: background var(--transition-fast);
}

.setting-item.clickable:active {
  background: var(--color-surface-2);
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

/* QR Code Dialog */
.qr-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.qr-dialog {
  position: relative;
  background: var(--color-surface);
  border-radius: var(--radius-xl, 20px);
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-xs);
  max-width: 300px;
  width: 85%;
  box-shadow: var(--shadow-lg, 0 8px 32px rgba(0,0,0,0.3));
}

.qr-close {
  position: absolute;
  top: 10px;
  right: 14px;
  font-size: 18px;
  color: var(--color-text-secondary);
  cursor: pointer;
  line-height: 1;
}

.qr-title {
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text);
}

.qr-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.qr-image {
  width: 220px;
  height: 220px;
  margin: var(--space-xs) 0;
}
</style>
