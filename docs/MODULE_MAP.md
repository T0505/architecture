# 功能映射

## 打工仔启动器 -> 新架构

| 原功能 | 新模块 | 新实现策略 |
| --- | --- | --- |
| `MainActivity` 启动器 | `app`, `feature:launcher` | 宿主首页 + 功能状态面板 |
| `StarterViewModel` | `feature:launcher` | 转成 launcher view model，订阅模块状态 |
| `App/JarvisAppProxy` | `app`, `feature:agent` | 初始化只保留配置入口和 agent client |
| `AgentService` | `feature:agent`, `core:ipc` | 改成统一 `AgentModule`，通过 `CommandRouter` 处理命令 |
| `UPDService` | `feature:agent`, `core:scheduling` | 改成配置同步 + 版本检查 + 前台可见状态 |
| `AgentLoader` | `feature:agent`, `core:storage` | 仅允许签名包/版本包更新，禁止任意动态执行 |
| `ServiceStatusHolder` | `feature:agent` | 日志上报、节点状态、任务状态 |
| `QTermux` | `feature:terminal` | 受控终端 session，默认 debug/白名单 |
| keepalive broadcast/job/file lock/one pixel | `core:scheduling` | 替换成用户可见前台服务 + WorkManager，不做隐藏保活 |
| `QFileProvider` | `core:storage` | 统一 FileProvider 和 scoped storage |
| Qiniu SDK | `feature:agent` 或 `data` | 只保留需要的网络/存储能力 |

## 月光宝盒 -> 新架构

| 原功能 | 新模块 | 新实现策略 |
| --- | --- | --- |
| `WelcomeActivity/MainActivity` | `app`, `feature:launcher` | 首页、权限页、脚本项目入口 |
| `MainService` | `core:service`, `core:ipc` | 主控制服务，转为模块注册和状态编排 |
| `NativeService/Native` | `native-bridge` | JNI wrapper 独立模块，运行在 `:engine` |
| `PluginService` | `feature:plugin` | 签名插件、独立进程、生命周期接口 |
| `ScriptMain/ScriptProxy` | `feature:script` | 脚本运行器和脚本代理 |
| `LuaEngine/LuaNative` | `feature:script`, `native-bridge` | Lua bridge，脚本 API 重新定义 |
| `AccessibilityService` | `feature:automation` | 用户授权的 UI 自动化能力 |
| `UiAutoMain/Ui*Native` | `feature:automation` | selector/object/action 抽象 |
| `ImeInput` | `feature:input` | 输入法服务和文本提交 |
| `Engine` | `native-bridge` | native engine 生命周期和事件桥 |
| `ImageRender/ImageUtils/SharedMem` | `feature:vision`, `native-bridge` | 截图、图像 buffer、共享内存 |
| `PaddleOcr/YoloV5` | `feature:vision` | OCR 和目标检测能力 |
| `assets/lua.zip/script.lrj` | `core:storage`, `feature:script` | 脚本包导入，不把字节码当源码依赖 |
| `assets/models.zip/paddle/*` | `feature:vision`, `core:storage` | 模型包版本化管理 |
| `BootBroadCastReceiver/JobSchedulerService` | `core:scheduling` | 用户授权开机启动和定时任务 |

## 被替换的实现方式

| 原实现类型 | 新实现 |
| --- | --- |
| 隐藏保活、一像素、文件锁 | 前台服务、WorkManager、用户可见通知 |
| 任意 shell/系统命令桥 | 命令白名单 + debug build 限制 + 审计日志 |
| 任意动态插件执行 | 签名插件 + 版本白名单 + 独立进程 |
| 散落 `sendCmd` 数字协议 | 统一 `RuntimeCommand` |
| native 直接全局加载 | `NativeBridge` 集中加载和健康检查 |
| 默认申请大量敏感权限 | 按功能模块延迟申请 |

