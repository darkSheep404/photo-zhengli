import { ref } from 'vue'
import { Capacitor } from '@capacitor/core'

export function usePermissions() {
  const hasPermission = ref(false)
  const permissionDenied = ref(false)

  async function requestPermissions(): Promise<boolean> {
    if (!Capacitor.isNativePlatform()) {
      // Web 环境下模拟授权
      hasPermission.value = true
      return true
    }

    try {
      // 通过 @capacitor-community/media 插件请求权限
      const { Media } = await import('@capacitor-community/media')
      const result = await Media.getMedias({ quantity: 1 })
      if (result.medias) {
        hasPermission.value = true
        return true
      }
    } catch (e: unknown) {
      const error = e as { message?: string }
      if (error.message?.includes('denied') || error.message?.includes('permission')) {
        permissionDenied.value = true
      }
      // 权限可能未授予，尝试请求
      try {
        const { Media } = await import('@capacitor-community/media')
        await Media.getAlbums()
        hasPermission.value = true
        return true
      } catch {
        permissionDenied.value = true
        hasPermission.value = false
        return false
      }
    }

    return false
  }

  return {
    hasPermission,
    permissionDenied,
    requestPermissions,
  }
}
