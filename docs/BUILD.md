# 构建说明

## 推荐环境

- IDE：Android Studio
- JDK：Android Studio bundled JDK 或 JDK 17
- Gradle：项目 wrapper 指定 `8.10.2`
- Android SDK：Android SDK Platform 35

## Android Studio 操作

1. 打开 `C:\work\merged-apk-architecture`。
2. 点击 `Sync Project with Gradle Files`。
3. 点击 `Build > Build APK(s)`。

如 Android Studio 提示 SDK 缺失，打开 `File > Settings > Languages & Frameworks > Android SDK`，确认：

1. 在 SDK Platforms 勾选 `Android 15.0 (API 35)`。
2. 在 SDK Tools 勾选：
   - Android SDK Build-Tools
   - Android SDK Platform-Tools
   - Android SDK Command-line Tools

## 命令行操作

SDK 和 Gradle wrapper 准备好后，在项目根目录运行：

```powershell
.\gradlew.bat :app:assembleDebug
```

输出 APK 默认在：

```text
app\build\outputs\apk\debug\app-debug.apk
```

## 当前机器状态

Android Studio 已安装 SDK 到：

```text
C:\Users\Administrator\AppData\Local\Android\Sdk
```

当前只看到 `android-36.1`，项目目前使用 `compileSdk = 35`。所以需要在 SDK Manager 里补装 API 35，或者后续把工程升级到支持 `android-36.1` 的 Android Gradle Plugin/DSL。

2026-06-25 更新：已通过 Android Studio 自带 `sdklib` 安装：

```text
platforms;android-35
build-tools;35.0.1
```

Codex shell 中执行完整 Gradle 构建时，当前环境会阻止 Gradle client 连接本地 daemon 的 `127.0.0.1` 端口；这不是项目代码错误。请优先在 Android Studio 内执行 Gradle Sync 和 Build。
