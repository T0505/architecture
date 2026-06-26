package com.merged.automation

import com.merged.core.model.Capability
import com.merged.core.model.RiskLevel
import com.merged.core.model.RuntimeCommand
import com.merged.core.model.RuntimeResult
import com.merged.core.service.RuntimeModule

class AutomationModule : RuntimeModule {
    override val capability = Capability(
        id = "automation",
        name = "User-authorized accessibility automation",
        actions = setOf("queryNode", "performAction", "setAccessibilityState")
    )

    override val riskByAction = mapOf(
        "queryNode" to RiskLevel.MEDIUM,
        "performAction" to RiskLevel.HIGH,
        "setAccessibilityState" to RiskLevel.HIGH
    )

    override fun handle(command: RuntimeCommand): RuntimeResult {
        return when (command.action) {
            "queryNode" -> RuntimeResult.ok("accessibility node query requested")
            "performAction" -> RuntimeResult.ok("accessibility action accepted")
            "setAccessibilityState" -> RuntimeResult.ok("accessibility state update requested")
            else -> RuntimeResult.error("unsupported automation action: ${command.action}")
        }
    }
}

