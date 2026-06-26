package com.merged.app

import android.app.Application
import com.merged.agent.AgentModule
import com.merged.automation.AutomationModule
import com.merged.core.ipc.CommandRouter
import com.merged.core.ipc.DefaultCommandPolicy
import com.merged.core.service.ModuleRegistry
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
    val router = CommandRouter(DefaultCommandPolicy())
    val registry = ModuleRegistry()

    override fun onCreate() {
        super.onCreate()

        registry.register(StorageModule(filesDir))
        registry.register(SchedulingModule())
        registry.register(LauncherModule())
        registry.register(AgentModule())
        registry.register(NativeBridgeModule())
        registry.register(ScriptModule())
        registry.register(AutomationModule())
        registry.register(VisionModule())
        registry.register(PluginModule())
        registry.register(TerminalModule())
        registry.register(InputModule())
        registry.attachTo(router)
    }
}

