import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Photo, PhotoDecision, Album } from '@/types/photo'

export interface CleanupConfig {
  scope: 'all' | 'album'
  albumIds: string[]
  sortOrder: 'oldest' | 'newest' | 'random'
  batchSize: number
}

export const useCleanupStore = defineStore('cleanup', () => {
  const decisions = ref<PhotoDecision[]>([])
  const currentPhotoIndex = ref(0)
  const photos = ref<Photo[]>([])
  const selectedMonth = ref<string>('')
  const cleanupConfig = ref<CleanupConfig>({
    scope: 'all',
    albumIds: [],
    sortOrder: 'oldest',
    batchSize: 50,
  })

  const deleteList = computed(() => decisions.value.filter(d => d.action === 'delete'))
  const moveList = computed(() => decisions.value.filter(d => d.action === 'move'))
  const deleteCount = computed(() => deleteList.value.length)
  const moveCount = computed(() => moveList.value.length)
  const totalSpaceToFree = computed(() =>
    deleteList.value.reduce((sum, d) => sum + d.photo.size, 0)
  )

  function markDelete(photo: Photo) {
    const idx = decisions.value.findIndex(d => d.photo.id === photo.id)
    const decision: PhotoDecision = { photo, action: 'delete' }
    if (idx !== -1) {
      decisions.value[idx] = decision
    } else {
      decisions.value.push(decision)
    }
  }

  function markMove(photo: Photo, album: Album) {
    const idx = decisions.value.findIndex(d => d.photo.id === photo.id)
    const decision: PhotoDecision = { photo, action: 'move', targetAlbum: album }
    if (idx !== -1) {
      decisions.value[idx] = decision
    } else {
      decisions.value.push(decision)
    }
  }

  function undoLast(): PhotoDecision | null {
    return decisions.value.pop() ?? null
  }

  function getDecision(photoId: string): PhotoDecision | undefined {
    return decisions.value.find(d => d.photo.id === photoId)
  }

  function clearAll() {
    decisions.value = []
    currentPhotoIndex.value = 0
  }

  function setPhotos(list: Photo[]) {
    photos.value = list
  }

  return {
    decisions,
    currentPhotoIndex,
    photos,
    selectedMonth,
    cleanupConfig,
    deleteList,
    moveList,
    deleteCount,
    moveCount,
    totalSpaceToFree,
    markDelete,
    markMove,
    undoLast,
    getDecision,
    clearAll,
    setPhotos,
  }
})
