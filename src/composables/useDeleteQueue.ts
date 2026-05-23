import { ref, computed } from 'vue'
import type { Photo, PhotoDecision, CleanupStats } from '@/types/photo'

export function useDeleteQueue() {
  const queue = ref<PhotoDecision[]>([])

  const deleteList = computed(() => queue.value.filter(d => d.action === 'delete'))
  const moveList = computed(() => queue.value.filter(d => d.action === 'move'))

  const stats = computed<CleanupStats>(() => {
    const toDelete = deleteList.value.length
    const toMove = moveList.value.length
    const total = queue.value.length
    return {
      total,
      processed: total,
      toDelete,
      toMove,
      toKeep: 0,
      spaceToFree: deleteList.value.reduce((sum, d) => sum + d.photo.size, 0),
    }
  })

  function markDelete(photo: Photo) {
    const existing = queue.value.findIndex(d => d.photo.id === photo.id)
    if (existing !== -1) {
      queue.value[existing] = { photo, action: 'delete' }
    } else {
      queue.value.push({ photo, action: 'delete' })
    }
  }

  function markMove(photo: Photo, targetAlbum: { id: string; name: string; count: number; coverUri: string }) {
    const existing = queue.value.findIndex(d => d.photo.id === photo.id)
    if (existing !== -1) {
      queue.value[existing] = { photo, action: 'move', targetAlbum }
    } else {
      queue.value.push({ photo, action: 'move', targetAlbum })
    }
  }

  function undoLast(): PhotoDecision | null {
    return queue.value.pop() ?? null
  }

  function undoPhoto(photoId: string) {
    const idx = queue.value.findIndex(d => d.photo.id === photoId)
    if (idx !== -1) {
      queue.value.splice(idx, 1)
    }
  }

  function getDecision(photoId: string): PhotoDecision | undefined {
    return queue.value.find(d => d.photo.id === photoId)
  }

  function clearQueue() {
    queue.value = []
  }

  return {
    queue,
    deleteList,
    moveList,
    stats,
    markDelete,
    markMove,
    undoLast,
    undoPhoto,
    getDecision,
    clearQueue,
  }
}
