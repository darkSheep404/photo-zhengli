const STORAGE_KEY = 'photo-zhengli-excluded-albums'

interface ExcludedAlbum {
  id: string
  name: string
}

function loadExcluded(): ExcludedAlbum[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw)
  } catch {}
  return []
}

function saveExcluded(albums: ExcludedAlbum[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(albums))
}

export function useExcludedAlbums() {
  function getExcludedAlbums(): ExcludedAlbum[] {
    return loadExcluded()
  }

  function getExcludedIds(): Set<string> {
    return new Set(loadExcluded().map(a => a.id))
  }

  function isExcluded(albumId: string): boolean {
    return loadExcluded().some(a => a.id === albumId)
  }

  function toggleExclude(albumId: string, albumName: string): boolean {
    const list = loadExcluded()
    const idx = list.findIndex(a => a.id === albumId)
    if (idx >= 0) {
      list.splice(idx, 1)
      saveExcluded(list)
      return false // no longer excluded
    } else {
      list.push({ id: albumId, name: albumName })
      saveExcluded(list)
      return true // now excluded
    }
  }

  function removeExclude(albumId: string) {
    const list = loadExcluded().filter(a => a.id !== albumId)
    saveExcluded(list)
  }

  function clearExcluded() {
    localStorage.removeItem(STORAGE_KEY)
  }

  return {
    getExcludedAlbums,
    getExcludedIds,
    isExcluded,
    toggleExclude,
    removeExclude,
    clearExcluded,
  }
}
