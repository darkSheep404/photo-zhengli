<template>
  <div class="app-layout">
    <div class="app-content">
      <router-view />
    </div>
    <TabBar v-if="showTabs" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import TabBar from '@/components/TabBar.vue'

const route = useRoute()
const showTabs = computed(() => route.meta.showTabs !== false)
</script>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.app-content {
  flex: 1;
  overflow: hidden;
}
</style>
