const STORAGE_KEY = 'photo-zhengli-favorite-albums'

interface FavoriteAlbum {
  id: string
  name: string
}

function loadFavorites(): FavoriteAlbum[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) return JSON.parse(raw)
  } catch {}
  return []
}

function saveFavorites(albums: FavoriteAlbum[]) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(albums))
}

export function useFavoriteAlbums() {
  function getFavoriteAlbums(): FavoriteAlbum[] {
    return loadFavorites()
  }

  function getFavoriteIds(): Set<string> {
    return new Set(loadFavorites().map(album => album.id))
  }

  function toggleFavorite(albumId: string, albumName: string): boolean {
    const favorites = loadFavorites()
    const index = favorites.findIndex(album => album.id === albumId)

    if (index >= 0) {
      favorites.splice(index, 1)
      saveFavorites(favorites)
      return false
    }

    favorites.push({ id: albumId, name: albumName })
    saveFavorites(favorites)
    return true
  }

  return {
    getFavoriteAlbums,
    getFavoriteIds,
    toggleFavorite,
  }
}