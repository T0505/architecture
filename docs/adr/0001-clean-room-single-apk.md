# ADR 0001: Clean-Room Single APK Multi-Module

## 决策

采用 clean-room 单 APK、多 Gradle module、多进程隔离架构。

## 背景

两个 APK 的职责不同：

- 一个偏启动器、UPD/Agent、QTermux、保活。
- 一个偏脚本引擎、native engine、UI 自动化、OCR、插件和输入控制。

直接合并反编译源码会带来 class/resource/provider 冲突、native 加载冲突、命令协议冲突和权限失控。

## 方案

用一个 Android application module 作为宿主，功能拆成内部 library modules。所有能力注册到 `ModuleRegistry`，所有跨模块动作经过 `CommandRouter`。

## 理由

- 可替换：后端、脚本引擎、OCR、插件都能独立替换。
- 可调试：哪个模块崩溃就重启哪个进程。
- 可控权：权限和高风险动作集中治理。
- 可迭代：一个月内可以先做核心链路，再逐步补 OCR、插件、终端等重功能。
- 可测试：模块接口稳定后，可以写单元测试和集成测试。

## 后果

短期需要写一层接口和适配器，不能最快拼出一个能跑的混合包。但长期维护成本低很多，也能避免两套 APK 的隐式命令互相影响。

