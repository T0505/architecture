# 统一命令模型

## 设计目标

合并后不能继续保留多套命令协议，否则会出现同号命令冲突、权限绕过和日志不可追踪。统一命令模型应满足：

- 所有模块命令都经过 `CommandRouter`。
- 每条命令有 target/action/requestId/source。
- 高风险 action 必须命中白名单。
- 所有命令返回结构化 `RuntimeResult`。
- 所有命令都能审计。

## 命令分类

| target | action 示例 | 来源功能 |
| --- | --- | --- |
| `agent` | `syncConfig`, `checkUpdate`, `uploadLog`, `queryNode` | 打工仔 UPD/Agent |
| `launcher` | `queryStatus`, `openPanel` | 两边 UI/启动器 |
| `script` | `installProject`, `start`, `stop`, `pause`, `resume`, `sendEvent` | 月光脚本 |
| `automation` | `queryNode`, `performAction`, `setAccessibilityState` | 月光 UI 自动化 |
| `vision` | `capture`, `ocr`, `detect`, `updateModel` | 月光 OCR/图像 |
| `plugin` | `install`, `start`, `stop`, `sendEvent`, `openActivity` | 月光插件 |
| `terminal` | `openSession`, `write`, `read`, `closeSession` | 打工仔 QTermux |
| `input` | `sendKey`, `sendText`, `sendTouch` | 月光 IME/input |

## 风险分级

| 风险 | 处理方式 |
| --- | --- |
| Low | 状态查询、日志查询、UI 打开，可直接执行 |
| Medium | 脚本启动、OCR、模型加载、插件 UI，需要模块权限 |
| High | 输入事件、无障碍动作、插件加载、终端写入，需要用户显式授权和白名单 |
| Restricted | 任意 shell、root、短信、电话、静默安装、隐藏保活，不作为默认能力实现 |

## 路由规则

1. `CommandRouter` 根据 `target` 找到 `RuntimeModule`。
2. `CommandPolicy` 检查调用来源、用户授权、action 风险、参数完整性。
3. `RuntimeModule` 执行 action。
4. `AuditSink` 记录 requestId、target、action、结果和耗时。

