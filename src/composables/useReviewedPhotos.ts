const STORAGE_KEY = 'photo-zhengli-reviewed'

function loadIds(): Set<string> {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return new Set(JSON.parse(raw))
  } catch {}
  return new Set()
}

function saveIds(ids: Set<string>) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify([...ids]))
}

export function useReviewedPhotos() {
  function addReviewedIds(photoIds: string[]) {
    const ids = loadIds()
    for (const id of photoIds) {
      ids.add(id)
    }
    saveIds(ids)
  }

  function isReviewed(photoId: string): boolean {
    return loadIds().has(photoId)
  }

  function getReviewedIds(): Set<string> {
    return loadIds()
  }

  function clearReviewed() {
    localStorage.removeItem(STORAGE_KEY)
  }

  function getReviewedCount(): number {
    return loadIds().size
  }

  return {
    addReviewedIds,
    isReviewed,
    getReviewedIds,
    clearReviewed,
    getReviewedCount,
  }
}
