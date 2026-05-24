import { registerPlugin } from '@capacitor/core'

export interface MediaPhoto {
  id: string
  contentUri: string
  filename: string
  dateAdded: number
  dateModified: number
  size: number
  width: number
  height: number
  albumId: string
  albumName: string
}

export interface MediaAlbum {
  id: string
  name: string
  count: number
  coverUri: string
}

export interface GetPhotosOptions {
  quantity?: number
  offset?: number
  ascending?: boolean
  albumId?: string
}

export interface GetPhotosResult {
  photos: MediaPhoto[]
}

export interface GetAlbumsResult {
  albums: MediaAlbum[]
}

export interface PermissionResult {
  granted: boolean
}

export interface MediaAccessPluginInterface {
  checkPermissions(): Promise<PermissionResult>
  requestPermissions(): Promise<PermissionResult>
  getPhotos(options: GetPhotosOptions): Promise<GetPhotosResult>
  getPhotoCount(): Promise<{ count: number }>
  getAlbums(): Promise<GetAlbumsResult>
}

export const MediaAccessPlugin = registerPlugin<MediaAccessPluginInterface>('MediaAccessPlugin')
