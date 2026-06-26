package com.merged.plugin

import com.merged.core.model.Capability
import com.merged.core.model.RiskLevel
import com.merged.core.model.RuntimeCommand
import com.merged.core.model.RuntimeResult
import com.merged.core.service.RuntimeModule

class PluginModule : RuntimeModule {
    override val capability = Capability(
        id = "plugin",
        name = "Signed plugin lifecycle",
        actions = setOf("install", "start", "stop", "sendEvent", "openActivity")
    )

    override val riskByAction = mapOf(
        "install" to RiskLevel.HIGH,
        "start" to RiskLevel.HIGH,
        "stop" to RiskLevel.MEDIUM,
        "sendEvent" to RiskLevel.MEDIUM,
        "openActivity" to RiskLevel.MEDIUM
    )

    override fun handle(command: RuntimeCommand): RuntimeResult {
        return when (command.action) {
            "install" -> RuntimeResult.ok("plugin install requested")
            "start" -> RuntimeResult.ok("plugin start requested")
            "stop" -> RuntimeResult.ok("plugin stop requested")
            "sendEvent" -> RuntimeResult.ok("plugin event accepted")
            "openActivity" -> RuntimeResult.ok("plugin activity requested")
            else -> RuntimeResult.error("unsupported plugin action: ${command.action}")
        }
    }
}

