# Manor Browser
 
 [**English**] | [**简体中文**](README_zh.md)
 
 ![Project Version](https://img.shields.io/badge/version-beta--0.0.1-blue)
 ![Engine](https://img.shields.io/badge/Engine-GeckoView--148-orange)
 ![License](https://img.shields.io/badge/License-Apache--2.0-green)
 
-**Manor Browser** is a modern Android web browser built on Mozilla's GeckoView (Firefox engine).
+**Manor Browser** is a modern Android web browser built on Mozilla's GeckoView (Firefox engine). It combines powerful performance with deep AI hosting, geeky controls, and an ultimate personalized experience.
 
 ## 🌟 Key Features
 
-### 1. Intelligent Hosting & AI Assistant
-*   **Manor AI Assistant**: An integrated, floating AI overlay that can read the current webpage, answer questions, and assist with browser actions.
-*   **Remote Server Communication**: Configure an external AI server address to enable complex page analysis, autonomous decision-making, and browsing tasks.
-
-### 2. Powered by Mozilla GeckoView
-*   **Core Engine**: Inherits the high privacy, performance, and rendering standards of the Firefox engine.
-*   **Extension Support**: Full support for installing and managing Firefox mobile extensions to customize your experience.
-
-### 3. Rich Aesthetic Experience
-*   **Dynamic Background System**: Beautiful live effects (Meteor, Rain, Snow, Aurora) and support for custom image backgrounds.
-*   **Modern Interaction**: Adheres to Material Design guidelines with support for full-screen mode and immersive browsing.
-*   **High-Efficiency Navigation**: Features a convenient **Sidebar** and a **Customizable Bottom-Left Button**, allowing one-tap access to bookmarks, extensions, or downloads for better single-handed usability.
-
-### 4. Advanced Utility Tools
-*   **Free Copy Mode**: Bypasses text selection and copy restrictions on many websites.
-*   **Deep Reader Mode**: Extracts main content with customizable font sizes, themes (Eye Care, Beige, Night), and styles.
-*   **Password Manager**: Securely saves, updates, and autofills your website credentials locally.
-*   **Cookie Management**: Dedicated management for Cookies and Site Data on a per-domain basis, supporting clearing and viewing.
+### 1. Intelligent Hosting & AI Assistant
+*   **Built-in AI Assistant**: A floating interactive window that can read, understand, and analyze the current webpage.
+*   **Autonomous Operation**: Supports AI-driven navigation, content search, UI structure parsing, click/input simulation, and tab management.
+*   **Remote Server Connection**: Supports custom AI server addresses (compatible with OpenAI and other major APIs) for complex automated decision-making and batch tasks.
+*   **Full-featured Toolset**: The AI can directly invoke browser core functions, such as clearing history, managing downloads, and executing custom scripts (JS Evaluation).
+
+### 2. Exclusive "Lord Mode"
+*   **Absolute Privacy Control**: When enabled, the browser will not record any search or browsing history at all.
+*   **Granular Permission Auditing**: Real-time warnings appear for website requests for Cookies or local storage, allowing users to decide whether to authorize (Allow/Always Deny).
+*   **Location Management**: Built-in restricted sites list for fine-grained control over geographic location permissions for specific websites.
+
+### 3. Efficient Tab & Engine Management
+*   **Advanced Tab Groups**: Save tabs to different groups, switch between groups quickly, and manage tabs efficiently with "Close all tabs in group."
+*   **16+ Major Search Engines**: Built-in support for Google, Bing, DuckDuckGo, Yahoo, Yandex, Ecosia, Brave, Startpage, Sogou, 360, Qwant, Naver, Seznam, Mojeek, MetaGer, and Baidu.
+*   **Smart Address Parsing**: Automatically recognizes search terms, domains, IPs, or .onion domains, and supports direct access to LAN addresses.
+
+### 4. Ultimate Personalization & Interaction
+*   **Dynamic Environments**: Features 7 particle background effects including Meteor Shower, Rain, Snow, Aurora, and Sakura Rain, plus support for custom image backgrounds.
+*   **Customizable Action Button**: The bottom-left button function can be selected by the user from: Home, Extensions, Bookmarks, History, Downloads, Desktop Mode, Add Bookmark, or Theme Toggle.
+*   **Immersive Experience**: Supports one-tap full-screen mode, following Material Design 3 guidelines with smooth micro-animations.
+
+### 5. Multilingual & Specialized Script Support
+*   **Global Coverage**: Extensive support for English, Chinese, Japanese, Korean, German, French, Italian, Russian, Portuguese, Arabic, Hindi, Vietnamese, Indonesian, Polish, Dutch, and more.
+*   **Unique Scripts**: Initial support for rare or specialized scripts such as Ryukyuan (Okinawan).
+
+### 6. Developer & Power User Tools
+*   **Remote Debugging Server**: Built-in HTTP JSON command server allowing direct control of the browser via external scripts (e.g., Python).
+*   **Free Copy Mode**: Bypass website text selection restrictions to freely extract any content.
+*   **Deep Reader Mode**: Extract main content with Eye Care, Beige, and Night themes, plus stepless font size adjustment.
+*   **Core-level Enhancements**: Full support for Firefox Mobile Extensions, with integrated Cookie and Password managers.
+*   **QR Scanner**: Native QR code scanning and link parsing tools.
 
 ---
 
 ## 🛠️ Tech Stack
-*   **Language**: Java
 *   **Core Engine**: Mozilla GeckoView
-*   **Build Info**: Compile SDK 36, Min SDK 26 (Android 8.0+)
-*   **Core Libraries**: OkHttp (Networking), Material Design (UI Framework)
+*   **Language**: Java
+*   **Networking**: OkHttp
+*   **UI Framework**: Material Design
+*   **Requirements**: Compile SDK 36, Min SDK 26 (Android 8.0+)
 
 ---
 
 ## 🚀 Getting Started
 
 ### Pre-built APK
-Go to the [Releases](https://github.com/Olsc/ManorBrowser/releases) page to download the latest APK for installation.
+Download the latest APK from the [Releases](https://github.com/Olsc/ManorBrowser/releases) page.
 
 ### Building from Source
 1.  Ensure you have Android Studio installed (Jellyfish or newer).
 2.  Clone the repository:
     ```bash
     git clone https://github.com/Olsc/ManorBrowser.git
     ```
-3.  Open the project in Android Studio.
-4.  Wait for Gradle sync to complete.
-5.  Click the **Run** button to deploy to your device or emulator.
-
----
-
-## 🔧 AI & Remote Control Configuration
-Configure your AI server address in **Settings -> Intelligent Hosting**. Once enabled, the browser will listen for instructions from your server, or you can interact with it via the built-in AI assistant.
+3.  Open the project in Android Studio and sync Gradle.
+4.  Connect your device and click **Run**.
 
 ---
 
 ## 🤝 Contributing
Contributions are always welcome!
*   **Report Bugs**: If you encounter crashes or unusual behavior.
*   **Feature Requests**: Share your ideas in GitHub Issues.
*   **Submit Code**: Fork the repo, create a branch, and submit a Pull Request.

## 🛡️ Privacy & Security
Manor Browser respects every user's privacy. The application itself does not collect any sensitive personal information, and all history and Cookies can be managed or destroyed locally.
For more details, please refer to the [User Privacy Agreement](app/src/main/assets/UserPrivacyAgreement_en.txt).

## 📄 License
This project is open-source under the [Apache License 2.0](LICENSE).

## 👤 Author
*   **Olsc** - [GitHub Profile](https://github.com/Olsc)
*   **Email**: OlscStudio@outlook.com

---
*Your private manor starts here.*

