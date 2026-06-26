package com.merged.app

import android.app.Application
import com.merged.agent.AgentModule
import com.merged.automation.AutomationModule
import com.merged.core.ipc.CommandRouter
import com.merged.core.ipc.DefaultCommandPolicy
import com.merged.core.service.ModuleRegistry
import com.merged.core.service.RuntimeStateStore
import com.merged.input.InputModule
import com.merged.launcher.LauncherModule
import com.merged.nativebridge.NativeBridgeModule
import com.merged.plugin.PluginModule
import com.merged.scheduling.SchedulingModule
import com.merged.script.ScriptModule
import com.merged.storage.StorageModule
import com.merged.terminal.TerminalModule
import com.merged.vision.VisionModule

class MergedRuntimeApp : Application() {
    lateinit var runtimeManager: RuntimeManager
        private set
    lateinit var stateStore: RuntimeStateStore
        private set

    val router by lazy {
        CommandRouter(
            policy = DefaultCommandPolicy(
                highRiskAllowList = setOf(
                    "automation.performAction",
                    "automation.setAccessibilityState",
                    "vision.capture",
                    "input.sendKey",
                    "input.sendText",
                    "input.sendTouch"
                )
            ),
            auditSink = stateStore
        )
    }
    val registry = ModuleRegistry()

    override fun onCreate() {
        super.onCreate()

        runtimeManager = RuntimeManager.create(this)
        stateStore = runtimeManager.stateStore

        registry.register(StorageModule(filesDir))
        registry.register(SchedulingModule())
        registry.register(LauncherModule(runtimeManager, stateStore, registry))
        registry.register(AgentModule(stateStore))
        registry.register(NativeBridgeModule())
        registry.register(ScriptModule())
        registry.register(AutomationModule())
        registry.register(VisionModule())
        registry.register(PluginModule())
        registry.register(TerminalModule())
        registry.register(InputModule())
        stateStore.registerModules(registry.modules())
        registry.attachTo(router)
        registry.modules().forEach { module ->
            stateStore.updateModuleState(
                id = module.capability.id,
                name = module.capability.name,
                state = "ready",
                detail = "Registered in command router"
            )
        }
        stateStore.appendLog("App", "Merged runtime application initialized")
    }
}
