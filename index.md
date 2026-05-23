# 📸 照片整理 (Photo Zhengli)

一款安卓照片清理 APP，帮助你快速浏览手机照片并决定保留、删除或移动到指定相册。

## 功能特性

- **快速浏览整理**：大图展示 + 底部缩略图条，一目了然
- **三键操作**：撤销 / 添加到相册 / 删除，操作简单高效
- **相册管理**：Bottom Sheet 快速选择目标相册，支持新建相册
- **安全删除**：删除的照片移入系统「最近删除」，30 天内可恢复
- **月份筛选**：按月份快速定位照片
- **照片统计**：首页展示总照片数、相册数、占用空间
- **CI/CD**：GitHub Actions 自动构建 APK，tag 推送自动发布 Release

## 技术架构

| 层级 | 技术 |
|------|------|
| UI 框架 | Vue 3.5 + TypeScript 5.7 |
| 构建工具 | Vite 6 |
| 状态管理 | Pinia 2.3 |
| 路由 | Vue Router 4.5 |
| 原生桥接 | Capacitor 6.2 |
| 照片访问 | @capacitor-community/media 6.0 |
| 原生插件 | MovePhotoPlugin / TrashPhotoPlugin (Java) |
| 目标平台 | Android 10-12 (兼容 HarmonyOS 4.2) |

## 项目结构

```
photo-zhengli/
├── src/
│   ├── views/              # 页面组件
│   │   ├── HomePage.vue        # 首页 - 统计 + 开始整理
│   │   ├── CleanupPage.vue     # 整理页 - 核心操作界面
│   │   ├── ReviewPage.vue      # 审核页 - 确认删除/移动
│   │   └── SettingsPage.vue    # 设置页
│   ├── components/         # UI 组件
│   │   ├── ActionButtons.vue   # 撤销/添加到/删除按钮
│   │   ├── AlbumSheet.vue      # 相册选择 Bottom Sheet
│   │   ├── MonthPicker.vue     # 月份筛选器
│   │   ├── ProgressBar.vue     # 进度条
│   │   └── ThumbnailStrip.vue  # 底部缩略图条
│   ├── composables/        # 组合式函数
│   │   ├── usePhotos.ts        # 照片加载与导航
│   │   ├── useAlbums.ts        # 相册管理
│   │   ├── useMovePhoto.ts     # 移动/删除原生桥接
│   │   ├── useDeleteQueue.ts   # 删除队列管理
│   │   └── usePermissions.ts   # 权限请求
│   ├── store/              # Pinia Store
│   │   └── cleanupStore.ts     # 整理状态管理
│   ├── types/              # TypeScript 类型
│   │   └── photo.ts            # Photo / Album 接口定义
│   ├── router/             # 路由配置
│   ├── styles/             # 全局样式
│   ├── App.vue
│   └── main.ts
├── android/                # Android 原生层
│   └── app/src/main/java/com/photozhengli/app/
│       ├── MainActivity.java
│       ├── MovePhotoPlugin.java    # 移动照片到相册
│       └── TrashPhotoPlugin.java   # 移入最近删除
├── .github/workflows/
│   └── build-apk.yml      # CI/CD 自动构建
├── capacitor.config.ts     # Capacitor 配置
├── package.json
├── vite.config.ts
└── tsconfig.json
```

## 快速开始

### 环境要求

- Node.js >= 20.19.0
- Android Studio (构建 APK)
- Java 17

### 安装与开发

```bash
# 安装依赖
npm install

# 本地开发预览（Web）
npm run dev

# 构建
npm run build

# 同步到 Android
npx cap sync android

# 在 Android Studio 中打开
npx cap open android
```

### 构建 APK

```bash
# Debug APK
cd android && ./gradlew assembleDebug

# Release APK
cd android && ./gradlew assembleRelease
```

APK 输出路径：`android/app/build/outputs/apk/`

### 自动构建

推送代码到 `main` 分支自动构建 debug APK。推送 `v*` tag 自动构建 release APK 并创建 GitHub Release。

```bash
git tag v0.1.0
git push origin v0.1.0
```

## 兼容性

| 系统 | 版本 | 支持状态 |
|------|------|---------|
| Android | 10 (API 29) | ✅ 完全支持 |
| Android | 11 (API 30) | ✅ 完全支持 |
| Android | 12 (API 31-32) | ✅ 完全支持 |
| Android | 13+ (API 33+) | ✅ 完全支持 |
| HarmonyOS | 4.2 | ✅ 兼容（基于 Android 10-12） |

## 版本历史

- **v0.1.0** - 初始版本，完整实现照片整理核心功能

## License

MIT
