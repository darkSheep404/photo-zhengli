import { registerPlugin } from '@capacitor/core'

interface ManageMediaStatus {
  supported: boolean
  granted: boolean
}

interface ManageMediaPluginInterface {
  getStatus(): Promise<ManageMediaStatus>
  requestPermission(): Promise<ManageMediaStatus>
}

const ManageMediaPlugin = registerPlugin<ManageMediaPluginInterface>('ManageMediaPlugin')

export async function getManageMediaStatus(): Promise<ManageMediaStatus> {
  return ManageMediaPlugin.getStatus()
}

export async function requestManageMediaPermission(): Promise<ManageMediaStatus> {
  return ManageMediaPlugin.requestPermission()
}