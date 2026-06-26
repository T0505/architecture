package com.merged.launcher

import com.merged.core.model.Capability
import com.merged.core.model.RiskLevel
import com.merged.core.model.RuntimeCommand
import com.merged.core.model.RuntimeResult
import com.merged.core.service.RuntimeModule

class LauncherModule : RuntimeModule {
    override val capability = Capability(
        id = "launcher",
        name = "Launcher and status panel",
        actions = setOf("queryStatus", "openPanel")
    )

    override val riskByAction = mapOf(
        "queryStatus" to RiskLevel.LOW,
        "openPanel" to RiskLevel.LOW
    )

    override fun handle(command: RuntimeCommand): RuntimeResult {
        return when (command.action) {
            "queryStatus" -> RuntimeResult.ok(data = mapOf("ready" to "true"))
            "openPanel" -> RuntimeResult.ok("launcher panel requested")
            else -> RuntimeResult.error("unsupported launcher action: ${command.action}")
        }
    }
}

