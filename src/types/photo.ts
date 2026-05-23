export interface Photo {
  id: string
  uri: string
  webPath: string
  filename: string
  albumId: string
  albumName: string
  width: number
  height: number
  size: number
  createdAt: number
  modifiedAt: number
}

export interface Album {
  id: string
  name: string
  count: number
  coverUri: string
}

export type PhotoAction = 'keep' | 'delete' | 'move'

export interface PhotoDecision {
  photo: Photo
  action: PhotoAction
  targetAlbum?: Album
}

export interface CleanupStats {
  total: number
  processed: number
  toDelete: number
  toMove: number
  toKeep: number
  spaceToFree: number
}
