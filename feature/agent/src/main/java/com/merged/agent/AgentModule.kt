package com.merged.agent

import com.merged.core.model.Capability
import com.merged.core.model.RiskLevel
import com.merged.core.model.RuntimeCommand
import com.merged.core.model.RuntimeResult
import com.merged.core.service.RuntimeModule

class AgentModule : RuntimeModule {
    override val capability = Capability(
        id = "agent",
        name = "Configuration, update and reporting agent",
        actions = setOf("syncConfig", "checkUpdate", "uploadLog", "queryNode")
    )

    override val riskByAction = mapOf(
        "syncConfig" to RiskLevel.MEDIUM,
        "checkUpdate" to RiskLevel.MEDIUM,
        "uploadLog" to RiskLevel.MEDIUM,
        "queryNode" to RiskLevel.LOW
    )

    override fun handle(command: RuntimeCommand): RuntimeResult {
        return when (command.action) {
            "queryNode" -> RuntimeResult.ok(data = mapOf("node" to "unbound"))
            "syncConfig" -> RuntimeResult.ok("config sync queued")
            "checkUpdate" -> RuntimeResult.ok("update check queued")
            "uploadLog" -> RuntimeResult.ok("log upload queued")
            else -> RuntimeResult.error("unsupported agent action: ${command.action}")
        }
    }
}

