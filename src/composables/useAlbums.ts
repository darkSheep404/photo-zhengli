import { ref } from 'vue'
import { Capacitor } from '@capacitor/core'
import type { Album } from '@/types/photo'
import { MediaAccessPlugin } from '@/plugins/mediaAccess'

export function useAlbums() {
  const albums = ref<Album[]>([])
  const loading = ref(false)

  async function loadAlbums(): Promise<void> {
    loading.value = true
    try {
      if (!Capacitor.isNativePlatform()) {
        albums.value = generateMockAlbums()
        return
      }

      const result = await MediaAccessPlugin.getAlbums()

      albums.value = (result.albums ?? []).map(a => ({
        id: a.id ?? '',
        name: a.name ?? '未知相册',
        count: a.count ?? 0,
        coverUri: a.coverUri ? Capacitor.convertFileSrc(a.coverUri) : '',
      }))
    } finally {
      loading.value = false
    }
  }

  async function createAlbum(name: string): Promise<Album | null> {
    if (!name.trim()) return null

    try {
      if (!Capacitor.isNativePlatform()) {
        const newAlbum: Album = {
          id: `album-${Date.now()}`,
          name: name.trim(),
          count: 0,
          coverUri: '',
        }
        albums.value.push(newAlbum)
        return newAlbum
      }

      const { Media } = await import('@capacitor-community/media')
      await Media.createAlbum({ name: name.trim() })

      // 重新加载相册列表获取新建相册的 ID
      await loadAlbums()
      return albums.value.find(a => a.name === name.trim()) ?? null
    } catch (e) {
      console.error('创建相册失败:', e)
      return null
    }
  }

  return {
    albums,
    loading,
    loadAlbums,
    createAlbum,
  }
}

function generateMockAlbums(): Album[] {
  return [
    { id: 'camera', name: '相机', count: 156, coverUri: '' },
    { id: 'screenshots', name: '截屏', count: 89, coverUri: '' },
    { id: 'wechat', name: '微信', count: 234, coverUri: '' },
    { id: 'travel', name: '旅行', count: 67, coverUri: '' },
    { id: 'food', name: '美食', count: 45, coverUri: '' },
  ]
}
