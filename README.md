# 素扉 (SuFei)

[![Android Studio](https://img.shields.io/badge/Android%20Studio-2024.2+-orange.svg?style=flat&logo=android-studio)](https://developer.android.com/studio)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.7+-green.svg?style=flat&logo=jetpack-compose)](https://developer.android.com/jetpack/compose)

**素扉**（SuFei）是一款专注于中国传统诗词沉浸式阅读的应用。其设计理念源于“素雅之色，开启文学之扉”，旨在打造一个无广告、无干扰、纯粹且宁静的数字诗集。

---

## ✨ 核心特性

- 📖 **沉浸式阅读**：极致的排版优化，支持衬线字体，模拟宣纸质感的阅读体验。
- 📅 **每日偶遇**：首页“今日”板块，基于智能算法每 10 分钟推荐一段经典的、语义完整的诗词金句。
- 🔍 **万卷搜寻**：三级过滤体系（朝代、词牌、标签），助你精准定位心头好。
- ❤️ **枕边私藏**：一键收藏心仪作品，打造个人专属的文学空间。
- 🎭 **智能解析**：精准识别诗、词、曲，并根据文体特征自动提取最精彩的完整段落展示。
- 🎨 **现代动效**：全站支持 Navigation 3 动效规范，包括优雅的淡入淡出及 Android 13+ 预测性返回手势。

---

## 🛠️ 技术栈

- **UI 框架**：[Jetpack Compose](https://developer.android.com/jetpack/compose) (声明式 UI)
- **导航**：[Navigation 3](https://developer.android.com/guide/navigation/navigation-3) (最新的 Compose 导航方案)
- **依赖注入**：[Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **本地存储**：[Room](https://developer.android.com/training/data-storage/room) (SQLite 封装)
- **数据流**：Kotlin Coroutines & Flow
- **偏好管理**：[Proto DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- **架构**：遵循 MVVM 架构及 Now in Android (NiA) 的响应式编程规范。

---

## 🎨 设计理念

项目核心遵循 **Material 3** 规范，并融入了中国传统美学：
- **留白**：大面积的背景留白，缓解视觉压力。
- **竖排**：首页金句采用传统竖排布局，标点符号经过特殊偏移处理。
- **色彩**：采用低饱和度的传统色（如“妃红”作为收藏色），适配 Material You 动态色彩。

---

## 🚀 快速开始

1. 克隆仓库：
   ```bash
   git clone https://github.com/wceng/SuFei.git
   ```
2. 使用最新版 **Android Studio Ladybug** 或更高版本打开项目。
3. 等待 Gradle 同步完成后，直接运行即可。

---

## 📄 开源协议

本项目遵循 [MIT License](LICENSE) 协议。
