# 照片整理 App 会话交接

更新时间：2026-08-25（v0.1.1）

## 当前状态

- 分支：`main`
- 最新提交：`8c50eed fix: 移除确认页面的清除全部按钮，避免误操作`
- 工作区：干净，已与 `origin/main` 同步
- 仓库：<https://github.com/darkSheep404/photo-zhengli>
- 运行平台：Android / HarmonyOS 4.2；Web 端仅用于开发预览。

## 技术与架构

- 前端：Vue 3、TypeScript、Vite、Pinia、Vue Router。
- 原生壳：Capacitor 6 Android。
- 最低 SDK 29，目标 SDK 34。
- 照片读取：自定义 Java `MediaAccessPlugin`，不要用 `@capacitor-community/media` 的读取接口；该依赖仅保留给创建相册。
- 移动照片：自定义 `MovePhotoPlugin`，通过 Android 11+ 的 `MediaStore.createWriteRequest()` 请求写入权限。
- 已保留照片、排除相册、主题等轻量状态使用 `localStorage` 持久化。

## 主题与 UI 约束

项目具有 iOS 风格与像素风两套主题，主题变量在 `src/styles/global.css`。

- 所有新增或修改的 UI 必须使用 `--color-*`、`--radius-*`、`--blur-*`、`--font-*` 等变量，不能硬编码主题色、圆角和模糊。
- 像素风必须保持方角、无模糊、像素边框、等宽字体、无过渡动画。
- 功能、修复和 UI 调整都要新增到 `docs/CHANGELOG.md` 顶部的最新批次。

## 已实现的核心功能

### 照片整理

- 全部照片、指定相册、已保留照片三种范围。
- 最旧、最新、随机三种排序；默认排序为随机。
- 随机全部照片会从 2-4 个随机 offset 分段抽样，避免只看到最新照片。
- 随机相册会拉取较大候选集后洗牌。
- 左右滑动、缩略图条、保留、删除、移动到相册、撤销均已实现。
- 删除采用最后审核并批量确认；移动采用即时操作。
- 保留、删除或移动任一照片后均可点击“结束整理”进入审核页；无删除操作也可以完成并记录结果。
- 完成后，已审阅照片 ID 会持久化；全部和相册模式会跳过已审阅照片。设置页可清空这些记录。

### 相册排除

- 点击相册始终进入该相册整理；长按（600ms）只打开“排除 / 恢复”确认弹窗，确认后才改变状态。
- 已排除相册仍可单独进入整理，卡片显示“已排除”；只有“全部照片”整理会跳过该相册。
- 相册支持“常用优先 / 名称 A-Z”排列。星标的常用相册同时会在移动照片面板第一组出现。
- 全部照片整理时会按 `albumId` 跳过已排除相册；单独进入某个相册时不会受该排除项干扰。
- 实现：`src/composables/useExcludedAlbums.ts`；过滤逻辑在 `src/composables/usePhotos.ts`。

### 照片详情与 EXIF

- 点按整理页主图打开详情浮层。
- 详情显示文件名、拍摄日期、分辨率、文件大小、所属相册。
- 原生端 `MediaAccessPlugin.getPhotoExif()` 使用 `androidx.exifinterface` 从图片 URI 读取 GPS、拍摄时间、相机品牌/型号。
- 前端按需获取 EXIF，并使用 OpenStreetMap Nominatim 将经纬度反向地理编码成“城市 + 区县”。
- EXIF 结果在前端内存中缓存；截图、微信图片等没有 GPS EXIF 时不显示位置，这是正常行为。
- 位置和相机信息各占独立行，以避免文字截断。
- 未实现城市分组：若要在进度栏按城市分组，不能预加载所有照片的 EXIF（真机开销大）；应设计增量缓存或用户主动扫描方案后再做。

### 设置与反馈

- 设置页支持 iOS / 像素风主题切换、清空已保留照片记录。
- “联系我们 / 反馈问题”会打开微信二维码弹窗。
- 二维码：`public/img/wechat-qr.png`，无圆角、减少内边距、右上角 `✕` 或点遮罩关闭。
- 已加入保存二维码到本地的功能。需要在 Android 真机上验证下载/保存行为是否符合系统权限策略。

### Android 打包与发布

- `android/debug.keystore` 已固定并提交；debug 与 release 都使用同一签名，因此后续 APK 可覆盖安装。
- `versionCode` 使用 CI 的 `VERSION_CODE`，即 GitHub Actions `run_number`；`versionName` 为 `1.0.<versionCode>`。
- 推送到 `main` 自动构建 debug APK 并上传 Artifact。
- 推送 `v*` tag 时另外生成 release APK 和 GitHub Release。
- 推送命令需显式走本机代理，且不要修改全局 Git 配置：

```powershell
git -c http.proxy=http://127.0.0.1:7890 -c https.proxy=http://127.0.0.1:7890 push origin main
```

## 关键文件

| 职责 | 文件 |
| --- | --- |
| 整理主流程 / 详情页 / EXIF 反向地理编码 | `src/views/CleanupPage.vue` |
| 整理配置与默认随机排序 | `src/views/CleanupConfigPage.vue` |
| 加载、排序、reviewed / excluded 过滤 | `src/composables/usePhotos.ts` |
| 已审阅照片持久化 | `src/composables/useReviewedPhotos.ts` |
| 排除相册持久化 | `src/composables/useExcludedAlbums.ts` |
| 相册浏览与长按排除 | `src/views/AlbumsPage.vue` |
| 设置页与二维码弹窗 | `src/views/SettingsPage.vue` |
| MediaStore、权限、相册、EXIF | `android/app/src/main/java/com/photozhengli/app/MediaAccessPlugin.java` |
| 移动照片原生实现 | `android/app/src/main/java/com/photozhengli/app/MovePhotoPlugin.java` |
| Capacitor TypeScript 插件声明 | `src/plugins/mediaAccess.ts` |
| 全局主题变量与像素风规则 | `src/styles/global.css` |
| CI APK 构建 | `.github/workflows/build-apk.yml` |

## 验证方式

本地 Web 编译：

```powershell
npm run build
```

同步 Android 后本地构建 debug APK：

```powershell
npx cap sync android
Set-Location android
.\gradlew.bat assembleDebug
```

优先真机验证：

1. 首次安装后，确认相册读取权限、全部/相册/已保留三种范围及随机排序。
2. 长按相册并确认排除后，用“全部照片”整理确认其图片不再出现；恢复后确认重新出现；验证常用星标和两种排列方式。
3. 详情页分别打开含 GPS 与不含 GPS 的照片，检查位置、相机信息和无数据状态。
4. 删除流程确认进入系统“最近删除”而非永久删除，重点验证 HarmonyOS 4.2。
5. 测试微信二维码弹窗的关闭和保存图片功能。
6. 切换两套主题，检查详情卡片、排除标签、二维码弹窗均符合主题规则。
7. 审核页选择“放弃本次整理”并确认后，重新进入整理，确认旧的删除、移动、保留标记不会残留；分别验证确认删除和无删除完成后的最近清理数量。

## 已知风险与待办

- `createTrashRequest` 在 HarmonyOS 4.2 的实际回收站行为仍需真机验证。
- Nominatim 依赖网络，可能限流或不可用；当前失败时仅不展示地点，不阻断详情页。
- 图片无 GPS EXIF、媒体库未返回 `BUCKET_ID` 等情况会导致缺少地点或相册信息，应优雅降级。
- PNG 预览异常曾被用户提及，但尚未获得可复现截图/错误日志；目前所有图片统一通过 `Capacitor.convertFileSrc(contentUri)` 显示。
- 待开发：重复照片检测、大文件扫描、截图识别、存储空间分析、模糊图检测、多选批量、压缩、自动扫描、扫描暂停恢复等。详见 `docs/CHANGELOG.md` 的 TODO。

## 新会话建议起点

1. 先运行 `git status --short` 确认没有用户本地修改。
2. 执行 `npm run build`，先处理可复现的 TypeScript 或 Vite 错误。
3. 若继续功能迭代，优先从用户提供的真机截图和具体路径入手，不要先广泛重构。
4. 做完每个用户可见功能后，更新 `docs/CHANGELOG.md`，运行构建，再提交与推送。