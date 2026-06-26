package com.merged.script

import com.merged.core.model.Capability
import com.merged.core.model.RiskLevel
import com.merged.core.model.RuntimeCommand
import com.merged.core.model.RuntimeResult
import com.merged.core.service.RuntimeModule

class ScriptModule : RuntimeModule {
    override val capability = Capability(
        id = "script",
        name = "Script runtime and project lifecycle",
        actions = setOf("installProject", "start", "stop", "pause", "resume", "sendEvent")
    )

    override val riskByAction = mapOf(
        "installProject" to RiskLevel.MEDIUM,
        "start" to RiskLevel.MEDIUM,
        "stop" to RiskLevel.MEDIUM,
        "pause" to RiskLevel.LOW,
        "resume" to RiskLevel.LOW,
        "sendEvent" to RiskLevel.MEDIUM
    )

    override fun handle(command: RuntimeCommand): RuntimeResult {
        return when (command.action) {
            "installProject" -> RuntimeResult.ok("script project staged")
            "start" -> RuntimeResult.ok("script start requested")
            "stop" -> RuntimeResult.ok("script stop requested")
            "pause" -> RuntimeResult.ok("script pause requested")
            "resume" -> RuntimeResult.ok("script resume requested")
            "sendEvent" -> RuntimeResult.ok("script event accepted")
            else -> RuntimeResult.error("unsupported script action: ${command.action}")
        }
    }
}

