package com.merged.terminal

import com.merged.core.model.Capability
import com.merged.core.model.RiskLevel
import com.merged.core.model.RuntimeCommand
import com.merged.core.model.RuntimeResult
import com.merged.core.service.RuntimeModule

class TerminalModule : RuntimeModule {
    override val capability = Capability(
        id = "terminal",
        name = "Controlled local terminal session",
        actions = setOf("openSession", "write", "read", "closeSession")
    )

    override val riskByAction = mapOf(
        "openSession" to RiskLevel.HIGH,
        "write" to RiskLevel.RESTRICTED,
        "read" to RiskLevel.HIGH,
        "closeSession" to RiskLevel.MEDIUM
    )

    override fun handle(command: RuntimeCommand): RuntimeResult {
        return when (command.action) {
            "openSession" -> RuntimeResult.ok("terminal session requested")
            "write" -> RuntimeResult.error("terminal write is restricted until a safe allow-list executor is implemented")
            "read" -> RuntimeResult.ok("terminal read requested")
            "closeSession" -> RuntimeResult.ok("terminal close requested")
            else -> RuntimeResult.error("unsupported terminal action: ${command.action}")
        }
    }
}

