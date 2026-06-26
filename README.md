# Single APK Merge Architecture

这个目录是一套 clean-room 架构骨架，用来把两个 APK 观察到的功能面重写到一个宿主 APK 里。

重点约束：

- 不直接复制反编译源码。
- 一个 Android 宿主 APK，内部用 Gradle module 拆功能。
- 脚本、自动化、视觉识别、插件、agent、终端能力都通过统一能力接口注册。
- 高风险能力必须走显式权限、签名校验、命令白名单和用户可见状态。

## 文档

- `docs/ARCHITECTURE.md`：底层架构、进程模型、模块分层和理由。
- `docs/MODULE_MAP.md`：两个 APK 功能点到新架构模块的映射。
- `docs/COMMAND_MODEL.md`：统一命令模型和安全边界。
- `docs/adr/0001-clean-room-single-apk.md`：为什么采用 clean-room 单 APK、多模块方案。

## 骨架

当前包含 Android Gradle 项目骨架和核心接口文件。它还不是完整 App，下一步应按模块逐个补实现。

