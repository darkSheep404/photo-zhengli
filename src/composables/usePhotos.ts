import { ref, computed } from 'vue'
import { Capacitor } from '@capacitor/core'
import type { Photo } from '@/types/photo'
import type { CleanupConfig } from '@/store/cleanupStore'
import { MediaAccessPlugin } from '@/plugins/mediaAccess'
import { useReviewedPhotos } from '@/composables/useReviewedPhotos'

export function usePhotos() {
  const photos = ref<Photo[]>([])
  const loading = ref(false)
  const currentIndex = ref(0)
  const hasMore = ref(true)

  const currentPhoto = computed(() => photos.value[currentIndex.value] ?? null)
  const totalCount = computed(() => photos.value.length)

  async function loadPhotos(quantity = 100, offset = 0): Promise<void> {
    loading.value = true
    try {
      if (!Capacitor.isNativePlatform()) {
        photos.value = generateMockPhotos(30)
        hasMore.value = false
        return
      }

      const result = await MediaAccessPlugin.getPhotos({
        quantity,
        offset,
        ascending: false,
      })

      const mapped: Photo[] = (result.photos ?? []).map(p => mapMediaPhoto(p))

      if (offset === 0) {
        photos.value = mapped
      } else {
        photos.value.push(...mapped)
      }

      hasMore.value = mapped.length >= quantity
    } finally {
      loading.value = false
    }
  }

  async function loadPhotosWithConfig(config: CleanupConfig): Promise<void> {
    loading.value = true
    try {
      if (!Capacitor.isNativePlatform()) {
        let mocks = generateMockPhotos(config.batchSize)
        mocks = applySortOrder(mocks, config.sortOrder)
        photos.value = mocks
        hasMore.value = false
        return
      }

      const ascending = config.sortOrder === 'oldest'

      if (config.scope === 'reviewed') {
        // 从已保留照片中加载：分页获取设备照片，过滤出已保留的
        const { getReviewedIds } = useReviewedPhotos()
        const reviewedIds = getReviewedIds()
        const collected: Photo[] = []
        let offset = 0
        const pageSize = 200

        while (collected.length < config.batchSize) {
          const result = await MediaAccessPlugin.getPhotos({
            quantity: pageSize,
            offset,
            ascending,
          })
          const mapped = (result.photos ?? []).map(p => mapMediaPhoto(p))
          if (mapped.length === 0) break

          for (const photo of mapped) {
            if (reviewedIds.has(photo.id)) {
              collected.push(photo)
              if (collected.length >= config.batchSize) break
            }
          }
          offset += pageSize
        }
        photos.value = applySortOrder(collected, config.sortOrder)
      } else if (config.scope === 'album' && config.albumIds.length > 0) {
        const allPhotos: Photo[] = []
        for (const albumId of config.albumIds) {
          const result = await MediaAccessPlugin.getPhotos({
            quantity: config.batchSize,
            ascending,
            albumId,
          })
          const mapped = (result.photos ?? []).map(p => mapMediaPhoto(p))
          allPhotos.push(...mapped)
        }
        photos.value = applySortOrder(allPhotos, config.sortOrder).slice(0, config.batchSize)
      } else {
        const result = await MediaAccessPlugin.getPhotos({
          quantity: config.batchSize,
          ascending,
        })
        photos.value = (result.photos ?? []).map(p => mapMediaPhoto(p))
      }

      if (config.sortOrder === 'random') {
        photos.value = applySortOrder(photos.value, 'random')
      }

      hasMore.value = photos.value.length >= config.batchSize
    } finally {
      loading.value = false
    }
  }

  function mapMediaPhoto(p: any): Photo {
    return {
      id: p.id ?? '',
      uri: p.contentUri ?? '',
      webPath: p.contentUri ? Capacitor.convertFileSrc(p.contentUri) : '',
      filename: p.filename ?? '',
      albumId: p.albumId ?? '',
      albumName: p.albumName ?? '',
      width: p.width ?? 0,
      height: p.height ?? 0,
      size: p.size ?? 0,
      createdAt: p.dateAdded ?? Date.now(),
      modifiedAt: p.dateModified ?? Date.now(),
    }
  }

  function applySortOrder(list: Photo[], order: CleanupConfig['sortOrder']): Photo[] {
    const sorted = [...list]
    switch (order) {
      case 'oldest':
        return sorted.sort((a, b) => a.createdAt - b.createdAt)
      case 'newest':
        return sorted.sort((a, b) => b.createdAt - a.createdAt)
      case 'random':
        // Fisher-Yates shuffle
        for (let i = sorted.length - 1; i > 0; i--) {
          const j = Math.floor(Math.random() * (i + 1));
          [sorted[i], sorted[j]] = [sorted[j], sorted[i]]
        }
        return sorted
    }
  }

  async function loadMore(): Promise<void> {
    if (!hasMore.value || loading.value) return
    await loadPhotos(100, photos.value.length)
  }

  function goToIndex(index: number) {
    if (index >= 0 && index < photos.value.length) {
      currentIndex.value = index
    }
  }

  function nextPhoto() {
    if (currentIndex.value < photos.value.length - 1) {
      currentIndex.value++
    }
  }

  function prevPhoto() {
    if (currentIndex.value > 0) {
      currentIndex.value--
    }
  }

  function removeFromList(photoId: string) {
    const idx = photos.value.findIndex(p => p.id === photoId)
    if (idx !== -1) {
      photos.value.splice(idx, 1)
      if (currentIndex.value >= photos.value.length) {
        currentIndex.value = Math.max(0, photos.value.length - 1)
      }
    }
  }

  return {
    photos,
    loading,
    currentIndex,
    currentPhoto,
    totalCount,
    hasMore,
    loadPhotos,
    loadPhotosWithConfig,
    loadMore,
    goToIndex,
    nextPhoto,
    prevPhoto,
    removeFromList,
  }
}

function generateMockPhotos(count: number): Photo[] {
  return Array.from({ length: count }, (_, i) => ({
    id: `mock-${i}`,
    uri: '',
    webPath: `https://picsum.photos/seed/${i}/800/600`,
    filename: `IMG_${String(i).padStart(4, '0')}.jpg`,
    albumId: 'camera',
    albumName: '相机',
    width: 800,
    height: 600,
    size: Math.floor(Math.random() * 5000000) + 500000,
    createdAt: Date.now() - i * 86400000,
    modifiedAt: Date.now() - i * 86400000,
  }))
}
