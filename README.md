# 素扉 (SuFei) — 数字诗集

<p align="center">
  <img src="https://img.shields.io/badge/Android-Ladybug+-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-1.7-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white" />
  <img src="https://img.shields.io/badge/Get_it_on-F--Droid-blue?style=for-the-badge&logo=f-droid" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" />
</p>

**素扉**（SuFei）是一款基于 **Material 3** 设计规范的极简中国传统诗词应用。它不只是一个工具，更是一个宁静的数字阅读空间。

---

## 📸 视觉预览 (Screenshots)

<p align="center">
  <img src="screenshots/home.png" width="13%" />
  <img src="screenshots/explore.png" width="13%" />
  <img src="screenshots/collection.png" width="13%" />
  <img src="screenshots/poem_detail.png" width="13%" />
  <img src="screenshots/poet_detail.png" width="13%" />
  <img src="screenshots/search_result.png" width="13%" />
  <img src="screenshots/settings.png" width="13%" />
</p>

> [!TIP]
> **设计特色**：首页采用传统**竖排布局**，配合衬线字体与妃红印章，还原古籍美学。全站支持 **Material You** 动态色彩。

---

## ✨ 核心特性

- 🏛️ **现代架构**：完全基于 **Now in Android** 的响应式编程模型，遵循 Clean Architecture。
- 📖 **沉浸式阅读**：模拟宣纸质感，支持衬线字体，针对长短句自动优化的排版算法。
- 🎙️ **随心朗读**：集成系统 TTS 引擎，支持诗词全文朗读及实时句子高亮，助你领略音韵之美。
- 📅 **每日偶遇**：智能提取算法，每 1 小时自动在首页推荐一段意境完整的经典诗句。
- 🔍 **万卷搜寻**：三级极简过滤（朝代/词牌/标签），毫秒级全文检索。
- 🎭 **文体感知**：自动识别诗/词/曲，针对“词”自动提取精华结拍（末尾句）进行展示。
- 🎨 **优雅动效**：基于 Navigation 3 实现全局淡入淡出，原生支持 Android 13+ **预测性返回**手势。
- 🔄 **状态保留**：深度优化导航逻辑，在顶级页面间切换时完美保留页面状态（如滚动位置）。

---

## 📚 数据集 (Dataset)

本应用内置了海量的中国传统文学数据库，所有数据均存储于本地 Room 数据库中，支持离线查询：

- 📜 **二十万余首**：涵盖诗、词、曲、文言文等多种体裁。
- 🏛️ **全朝代覆盖**：从先秦、两汉、魏晋、南北朝、唐、宋、元、明、清至近现代。
- 👤 **万名诗人**：收录了 10,000+ 位文学家的生平简介及其代表作品。
- 🏷️ **精细分类**：内置上千个意境标签与词牌名分类，助你精准触达心中所想。

---

## 🛠️ 技术栈 (Tech Stack)

| 维度 | 技术选型 |
| :--- | :--- |
| **UI** | Jetpack Compose (1.7+) |
| **Navigation** | Navigation 3 (Experimental Compose API) |
| **Splash** | Core SplashScreen API |
| **DI** | Hilt |
| **Database** | Room |
| **Storage** | Proto DataStore |
| **Concurrency** | Kotlin Coroutines & Flow |

---

## 📥 下载 (Download)

您可以从以下渠道获取最新版本：

- **GitHub Releases**: [点击前往](https://github.com/wceng/SuFei/releases)
- **F-Droid**: [在 F-Droid 上获取](https://f-droid.org/zh_Hans/packages/dev.wceng.sufei/)

---

## 🚀 快速开始

1. **环境**：确保你的 Android Studio 版本为 **Ladybug (2024.2.1)** 或更新。
2. **克隆**：`git clone https://github.com/wceng/SuFei.git`
3. **运行**：本项目使用 `libs.versions.toml` 管理依赖，直接 Sync Gradle 即可运行。

---

## 📄 开源协议

本项目基于 **MIT License** 开源。欢迎任何形式的 PR 和 Issue！

---

<p align="center"> 如果这个项目触动了你的文人情怀，请点一个 <b>Star</b> ⭐ 鼓励我们。 </p>
