import { CapacitorConfig } from '@capacitor/cli'

const config: CapacitorConfig = {
  appId: 'com.photozhengli.app',
  appName: '照片整理',
  webDir: 'dist',
  server: {
    androidScheme: 'https',
  },
}

export default config
