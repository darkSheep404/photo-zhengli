# 照片整理 APP — 开发变更记录

## Batch 6 功能增强（2026-05-25）

### 新功能
1. **相册排除**：相册页长按可排除相册，排除的相册在整理全部照片时自动跳过
2. **EXIF 信息展示**：照片详情页显示 GPS 位置（反向地理编码为城市名）和相机型号
3. **联系我们**：设置页新增微信二维码反馈入口

### UI 改进
4. **照片详情卡片重设计**：图标式分行布局，显示文件名、日期、分辨率、大小、相册、位置、相机
5. **二维码弹窗优化**：去掉关闭按钮，右上角 ✕ 关闭，无圆角，更大图片

### Bug 修复
6. **构建错误修复**：移除未使用的 `isExcluded` 导入（TS6133）
7. **copilot-instructions 更新**：新增主题适配和 changelog 规范

---

## Batch 5 反馈修复（2026-05-25）

### Bug 修复
1. **getPhotoCount 鸿蒙兼容**：`COUNT(*) AS count` 投影在鸿蒙系统不支持，改为 `cursor.getCount()`
2. **已整理照片重复出现**：全部/相册模式下自动排除已保留(reviewed)的照片，避免下次整理重复出现
3. **整理页默认排序未生效**：`CleanupConfigPage` 中 `sortOrder` 硬编码为 `'oldest'`，改为读取 store 默认值

### UI 改进
4. **启动页改为 logo**：splash screen 使用 `ic_launcher_foreground` 居中显示，白色背景，添加动画时长 800ms
5. **首页统计卡片对齐**：`17.8 MB` 等长文本不再换行（`white-space: nowrap` + `min-width: 0`）
6. **默认排序改为随机**：store 默认 `sortOrder` 从 `'oldest'` 改为 `'random'`

---

## 项目初始需求（2026-05-23）

### 核心需求
1. **照片扫描**：扫描手机中的所有照片
2. **滑动交互**：上下滑动进行保留/删除决策（后改为左右滑+底部按钮）
3. **移动到相册**：支持将照片移动到指定相册
4. **底部相册栏**：底部显示相册名称列表，带加号图标可新建相册
5. **兼容鸿蒙 4.2**：测试设备为华为 HarmonyOS 4.2（基于 Android 10-12）

### 技术选型决策
- **技术栈**：Capacitor + Vue 3 + Vite + TypeScript（纯 Web 开发体验）
- **平台**：仅 Android + 鸿蒙 4.2
- **无 AI 功能**：纯手动整理
- **删除策略**：延迟删除（先收集，最后确认批量执行）
- **移动策略**：即时移动（点击相册栏立即执行）
- **原生插件**：接受写少量 Java/Kotlin

### 参考 UI
- 参考百度一刻的照片整理交互
- 顶部：进度 `X/Y` + 月份筛选 + 待删除计数
- 中间：大图展示当前照片
- 底部：缩略图条 + 三按钮（撤销/添加到/删除）
- 最终采用：缩略图条 + 按钮并存，左右滑切换照片

### 额外功能需求
- 照片详情查看（点击放大）
- 按日期/大小排序整理
- 重复照片检测（待实现）
- 照片统计（占用空间、数量趋势）
- 清理空间提示

---

## 第一批实测反馈 & 修复（2026-05-24 batch1）

### 发现问题
1. `@capacitor-community/media` 的 `getPhotos` 在鸿蒙上不返回数据
2. 权限申请需要适配 Android 13+ `READ_MEDIA_IMAGES`

### 解决方案
- 编写自定义 `MediaAccessPlugin.java` 替代 `@capacitor-community/media` 的 getPhotos/getAlbums/权限管理
- 使用 Bundle-based 分页查询 + cursor fallback（兼容鸿蒙）
- `@capacitor-community/media` 仅保留用于 `createAlbum`

---

## 第二批实测反馈 & 修复（2026-05-24 batch2）

### 发现问题
1. 相册封面图片不显示（content:// URI 在 WebView 中不可用）
2. 照片移动到相册功能无响应

### 修复
1. **相册封面修复**：在 `useAlbums.ts` 中对 `coverUri` 应用 `Capacitor.convertFileSrc()` 转换
2. **照片移动修复**：重写 `MovePhotoPlugin.java`，使用 `ActivityResultLauncher` + `MediaStore.createWriteRequest()` 处理 Android 11+ 写权限

---

## 第三批功能需求（2026-05-24 batch3 — 8项）

用户在实际使用中提出的 8 项需求/反馈：

### #01 返回键 & 删除计数
- **需求**：返回键弹出确认对话框，点击删除计数跳转到审核页
- **实现**：`showQuitConfirm` 对话框，`ProgressBar` 的删除计数可点击触发 `@review`

### #02 滑动跳过自动标记保留
- **需求**：左滑查看下一张时，当前照片自动标记为"保留"
- **实现**：`onTouchEnd` 左滑时调用 `store.markKeep()`，`goToPhoto` 前进时也标记
- **约束**：`markKeep` 不覆盖已有的 delete/move 决策

### #03 UI 交互调整（已包含在其他项中）

### #04 相册封面图修复
- **问题**：相册列表封面图显示为裂图
- **修复**：`useAlbums.ts` 中 `Capacitor.convertFileSrc(a.coverUri)`

### #05 移动到相册修复
- **问题**：点击相册后照片不移动，无任何反应
- **修复**：重写 `MovePhotoPlugin.java`，使用 `ActivityResultLauncher<IntentSenderRequest>` + `MediaStore.createWriteRequest()` + pending call pattern

### #06 统计页展示保留/分类数量
- **需求**：统计仪表盘增加"已保留"卡片，历史记录显示保留数量
- **实现**：`StatsPage` 添加第 5 张卡片，`CleanupRecord` 增加 `keptCount` 字段

### #07 清理完成后继续下一批
- **需求**：删除完成弹窗增加"继续清理下一批"按钮
- **实现**：`ReviewPage.continueCleanup()`，保留 `cleanupConfig`，清空 decisions，导航到 `/cleanup/session`

### #08 回收站行为待验证
- **问题**：`createTrashRequest` 在鸿蒙上的实际行为待确认
- **状态**：列入 TODO

---

## 第四批优化（2026-05-24 batch4）

### 已保留照片持久化
- **需求**：保留的照片下次整理时能被识别和跳过
- **实现**：
  - `useReviewedPhotos.ts`：localStorage 持久化已审阅照片 ID 集合
  - `CleanupConfigPage`：新增"已保留照片"范围选项
  - `usePhotos.ts`：`scope === 'reviewed'` 时分页扫描过滤
  - `SettingsPage`：添加"清空已保留照片记录"功能
  - `ReviewPage`：完成整理时自动调用 `saveReviewedIds()`

### 缩略图条优化
- **触摸拖动**：添加 `onStripTouchStart/Move/End` 实现水平拖动滚动
- **状态标记**：缩略图上显示 delete（红）/ keep（绿）/ move（蓝）标签

### 清理记录增加时间
- **改动**：`date` 字段改用 `new Date().toLocaleString('zh-CN')` 输出完整日期时间
- **改动**：记录增加 `durationMs`，统计页显示耗时

---

## 第五批优化（2026-05-24 batch5）

### 清理数量自定义输入
- **需求**：原来只有预设的 20/50/100/200，现在支持自定义输入
- **实现**：`CleanupConfigPage` 新增"自定义"按钮 + number input（1-9999）
- **状态**：✅ 已完成

### App 图标
- **需求**：用项目 logo 图片作为 app 图标
- **实现**：从 `docs/img/logo/IMG_20260524_063206.jpg` 生成全密度 `ic_launcher.png`、`ic_launcher_round.png`、`ic_launcher_foreground.png`
- **密度**：mdpi(48) / hdpi(72) / xhdpi(96) / xxhdpi(144) / xxxhdpi(192)
- **状态**：✅ 已完成

### 移除清理页调试日志
- **需求**：清理页上方的绿色调试信息影响体验
- **实现**：移除 `debugMsg` 相关代码，仅保留错误 toast（`toastMsg`）
- **状态**：✅ 已完成

---

## 第六批优化（2026-05-24 batch6）

### APK 签名统一
- **问题**：每次 CI 构建的 debug APK 签名不同，无法覆盖安装
- **修复**：
  - 生成固定 `android/debug.keystore` 并提交到仓库
  - `build.gradle` 中 debug 和 release 都使用同一 keystore
  - 首次需卸载旧 APK，之后可覆盖更新
- **状态**：✅ 已完成

### 版本号自动递增
- **问题**：手机提示有更高版本
- **修复**：
  - `versionCode` = GitHub Actions `run_number`（每次构建自增）
  - `versionName` = `1.0.<versionCode>`
  - CI workflow 通过环境变量 `VERSION_CODE` 传递
- **状态**：✅ 已完成

### 随机模式改进
- **问题**：原来的随机只是对最新 N 张打乱顺序，看到的总是最新照片
- **改进**：
  - `scope=all`：分 2~4 段从不同随机 offset 取连续照片（覆盖不同时间段），使用 `getPhotoCount()` 获取总数
  - `scope=album`：取 3 倍 batchSize 照片后 shuffle 截取
  - `scope=reviewed`：分页过滤后 shuffle
  - 新增原生 `getPhotoCount` 方法（COUNT(*) 查询，高效）
- **状态**：✅ 已完成

### 结束整理流程优化
- **问题**：只有删除操作才能进入完成页，保留/归类无法结束整理
- **改进**：
  - 右上角按钮从"待删除(N)"改为"结束整理 ›"
  - 有任何决策（保留/删除/归类）时高亮显示
  - 审核页新增"已保留"照片列表（绿色边框）
  - 新增"完成整理"按钮（无删除时），记录统计并保存 reviewed IDs
  - 完成弹窗根据是否有删除自适应文案
- **状态**：✅ 已完成

### 顶部时间显示修复
- **问题**：顶部月份始终显示当前系统时间（2026年5月），而非照片实际时间
- **修复**：`ProgressBar` 接收 `photoTime` prop，显示当前照片的 `createdAt` 年月
- **状态**：✅ 已完成

---

## 待实现功能（TODO）

- [ ] 验证 `createTrashRequest` 在鸿蒙 4.2 上的实际回收站行为
- [ ] 重复照片检测（自动扫描相似/重复照片并提示清理）
- [ ] 大文件扫描（按文件大小排序，优先清理占用空间大的照片）
- [ ] 截图清理（自动识别截图并归类整理）
- [ ] 照片详情展示（显示 EXIF 信息、拍摄时间、地点等）
- [ ] 存储空间分析（饼图/柱状图展示各相册占用空间）
- [ ] 模糊照片检测（自动识别模糊、低质量照片）
- [ ] 多选批量操作
- [ ] 照片压缩（无损/有损压缩节省空间）
- [ ] 照片库自动扫描（打开 app 自动索引所有照片）
- [ ] 扫描暂停/恢复（保存扫描进度，下次继续）
- [ ] 上次扫描时间展示

---

## Git 提交记录

| Commit | 描述 |
|--------|------|
| 初始提交 | 项目搭建：Vue 3 + Capacitor + 5-Tab 导航 |
| 后续多次 | Phase 1-6 逐步实现 |
| `5373160` | batch3 前的最后提交 |
| batch3 | 8 项功能需求实现 |
| batch4 | reviewed 持久化、缩略图优化、记录时间 |
| `c855edd` | feat: custom batch size, app icon, remove debug overlay |
| `444c50d` | fix: use fixed debug keystore for consistent APK signing |
| `db664b5` | fix: unified signing for debug+release, auto-increment versionCode |
| `a18558b` | feat: random mode picks from different time periods (2-4 chunks) |
| `6187b3a` | feat: finish cleanup without deletion, show kept photos in review |
| `dedfb06` | feat: album random mode, update about.md with all new features |
| `224c2b5` | fix: show actual photo date instead of current system time |
