import { ref, computed } from 'vue'
import { Capacitor } from '@capacitor/core'
import type { Photo } from '@/types/photo'

// On Android, MediaAsset.identifier IS the file path

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
        // Web 环境模拟数据
        photos.value = generateMockPhotos(30)
        hasMore.value = false
        return
      }

      const { Media } = await import('@capacitor-community/media')
      const result = await Media.getMedias({
        quantity: quantity + offset, // Plugin doesn't support offset; request more and slice
        sort: [{ key: 'creationDate', ascending: false }],
        types: 'photos',
      })

      const allMedias = result.medias ?? []
      const sliced = allMedias.slice(offset)

      const mapped: Photo[] = sliced.map((m, i) => ({
        id: m.identifier ?? `photo-${offset + i}`,
        uri: m.identifier ?? '',
        webPath: m.data
          ? `data:image/jpeg;base64,${m.data}`
          : (m.identifier ? Capacitor.convertFileSrc(m.identifier) : ''),
        filename: `photo_${offset + i}.jpg`,
        albumId: '',
        albumName: '',
        width: 0,
        height: 0,
        size: 0,
        createdAt: m.creationDate ? new Date(m.creationDate).getTime() : Date.now(),
        modifiedAt: Date.now(),
      }))

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
