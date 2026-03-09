# Manor Browser (领域浏览器)

[**简体中文**] | [**English**](README.md)

![Project Version](https://img.shields.io/badge/version-beta--0.0.1-blue)
![Engine](https://img.shields.io/badge/Engine-GeckoView--148-orange)
![License](https://img.shields.io/badge/License-Apache--2.0-green)

**Manor Browser** (领域浏览器) 是一款基于 Mozilla GeckoView (Firefox) 内核开发的现代化 Android 浏览器。它不仅继承了火狐内核的强大性能，还融合了深度 AI 托管、极客操控与极致的个性化体验。

## 🌟 核心特色

### 1. 智能托管 & AI 助手 (Intelligent Hosting)
*   **内置 AI 助手**: 悬浮式交互窗口，可直接阅读、理解并实时分析当前网页。
*   **自动化操作**: 支持通过 AI 进行网页导航、内容搜索、UI 结构解析、点击/输入模拟以及标签页管理。
*   **远程服务器连接**: 支持配置自定义 AI 服务器地址（兼容 OpenAI 等主流 API），实现复杂的自动化决策与批量任务。
*   **全功能工具集**: AI 可直接调动浏览器底层功能，如清理历史、管理下载、执行自定义脚本 (JS Evaluation) 等。

### 2. 独创“领主模式” (Lord Mode)
*   **绝对隐私控制**: 开启后，浏览器将完全不记录任何搜索或浏览历史。
*   **行为粒度审核**: 针对网站的 Cookie 存储及本地数据请求，系统将实时弹出警告窗口，由用户决定是否授权（允许/始终拒绝）。
*   **地理位置管理**: 内置受限站点列表，支持对特定网站的地理位置权限进行精细化管控。

### 3. 高效标签与引擎管理
*   **高级标签组**: 支持将标签页保存至不同分组、在分组间快速切换，并提供“一键关闭组内所有标签”的高效管理方式。
*   **16+ 主流搜索引擎**: 内置 Google, Bing, DuckDuckGo, Yahoo, Yandex, Ecosia, Brave, Startpage, Sogou, 360, Qwant, Naver, Seznam, Mojeek, MetaGer 以及百度，支持随意切换。
*   **智能地址解析**: 自动识别搜索词、域名、IP 或 .onion 域名，支持局域网地址直达。

### 4. 极致个性化与交互
*   **动态环境背景**: 提供流星雨、雨天、雪天、极光、樱花雨等 7 种粒子背景特效，并支持自定义图片背景。
*   **功能自定按钮**: 底部左侧按钮功能可由用户在“首页、插件、书签、历史、下载、桌面模式、添加书签、主题切换”中任选其一。
*   **沉浸式体验**: 支持一键全屏模式，遵循 Material Design 3 规范，提供流畅的微动效。

### 5. 多语言与特殊文种支持
*   **广泛覆盖**: 已全面支持中、英、日、韩、德、法、意、俄、葡、阿、印、越、波、荷等多国语言。
*   **特色文种**: 内置对琉球语 (Ryukyuan) 等稀有或特殊文种的初步支持。

### 6. 开发者与进阶工具
*   **远程调试服务**: 内置基于 HTTP JSON 的命令服务器，允许通过外部脚本（如 Python）直接操控浏览器。
*   **自由复制模式**: 突破常规网页文字选择限制，自由提取任何内容。
*   **深度阅读器**: 提取正文并提供护眼、米黄、夜间三种配色，支持字号无级调节。
*   **内核级增强**: 完美支持 Firefox 移动版扩展 (Extensions)，内置完善的 Cookie 与密码管理器。
*   **二维码工具**: 原生集成二维码扫描与链接解析功能。

---

## 🛠️ 技术栈

*   **核心引擎**: Mozilla GeckoView
*   **编程语言**: Java
*   **网络通讯**: OkHttp
*   **UI 框架**: Material Design 3
*   **部署环境**: Compile SDK 36, Min SDK 26 (Android 8.0+)

---

## 🚀 快速开始

### 预构建版本
前往 [Releases](https://github.com/Olsc/ManorBrowser/releases) 页面下载最新的 APK 文件进行安装。

### 自行编译
1.  确保已安装 Android Studio (Jellyfish 或更高版本)。
2.  克隆仓库：
    ```bash
    git clone https://github.com/Olsc/ManorBrowser.git
    ```
3.  在 Android Studio 中打开项目并同步 Gradle。
4.  连接设备并点击 **Run** 运行。

---

## 🤝 参与贡献
我们欢迎任何形式的贡献！
*   **提交 Bug**: 如果发现应用崩溃或功能异常。
*   **功能建议**: 您可以在 GitHub Issues 中提出您的想法。
*   **提交代码**: 请 Fork 本仓库，创建一个分支，并提交 Pull Request。

## 🛡️ 隐私与安全
领域浏览器尊重每一位用户的隐私。应用本身不收集任何个人敏感信息，所有历史记录与 Cookie 均可在本地管理或销毁。
更多详情请参阅 [用户隐私协议](app/src/main/assets/legal/UserPrivacyAgreement_zh.txt)。

## 📄 许可证
本项目基于 [Apache License 2.0](LICENSE) 协议开源。

## 👤 作者
*   **Olsc** - [GitHub Profile](https://github.com/Olsc)
*   **Email**: OlscStudio@outlook.com

---
*您的私人领地，由此开启。*
