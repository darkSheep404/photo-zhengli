import { ref } from 'vue'
import { Capacitor } from '@capacitor/core'
import { MediaAccessPlugin } from '@/plugins/mediaAccess'

export function usePermissions() {
  const hasPermission = ref(false)
  const permissionDenied = ref(false)

  async function requestPermissions(): Promise<boolean> {
    if (!Capacitor.isNativePlatform()) {
      hasPermission.value = true
      return true
    }

    try {
      // Check existing permission first
      const checkResult = await MediaAccessPlugin.checkPermissions()
      if (checkResult.granted) {
        hasPermission.value = true
        return true
      }

      // Request permission via our custom plugin
      const reqResult = await MediaAccessPlugin.requestPermissions()
      if (reqResult.granted) {
        hasPermission.value = true
        return true
      }

      permissionDenied.value = true
      hasPermission.value = false
      return false
    } catch (e) {
      console.error('Permission request failed:', e)
      permissionDenied.value = true
      hasPermission.value = false
      return false
    }
  }

  return {
    hasPermission,
    permissionDenied,
    requestPermissions,
  }
}
