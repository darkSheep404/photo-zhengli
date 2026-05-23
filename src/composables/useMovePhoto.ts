import { Capacitor } from '@capacitor/core'
import { registerPlugin } from '@capacitor/core'

interface MovePhotoPluginInterface {
  movePhoto(options: { photoUri: string; albumName: string }): Promise<void>
}

interface TrashPhotoPluginInterface {
  trashPhotos(options: { photoUris: string[] }): Promise<{ freedBytes: number }>
}

const MovePhotoPlugin = registerPlugin<MovePhotoPluginInterface>('MovePhotoPlugin')
const TrashPhotoPlugin = registerPlugin<TrashPhotoPluginInterface>('TrashPhotoPlugin')

export async function movePhotoToAlbum(photoUri: string, albumName: string): Promise<void> {
  if (!Capacitor.isNativePlatform()) {
    // Web 环境模拟
    console.log(`[Mock] Moving ${photoUri} to album "${albumName}"`)
    await new Promise(r => setTimeout(r, 300))
    return
  }

  await MovePhotoPlugin.movePhoto({ photoUri, albumName })
}

export async function trashPhotos(photoUris: string[]): Promise<number> {
  if (!Capacitor.isNativePlatform()) {
    console.log(`[Mock] Trashing ${photoUris.length} photos`)
    await new Promise(r => setTimeout(r, 500))
    return photoUris.length * 2_000_000 // 模拟释放空间
  }

  const result = await TrashPhotoPlugin.trashPhotos({ photoUris })
  return result.freedBytes
}
