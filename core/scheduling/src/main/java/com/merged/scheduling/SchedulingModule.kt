package com.merged.scheduling

import com.merged.core.model.Capability
import com.merged.core.model.RiskLevel
import com.merged.core.model.RuntimeCommand
import com.merged.core.model.RuntimeResult
import com.merged.core.service.RuntimeModule

class SchedulingModule : RuntimeModule {
    override val capability = Capability(
        id = "scheduling",
        name = "Visible background scheduling",
        actions = setOf("query", "enableForeground", "disableForeground", "scheduleWork")
    )

    override val riskByAction = mapOf(
        "query" to RiskLevel.LOW,
        "enableForeground" to RiskLevel.MEDIUM,
        "disableForeground" to RiskLevel.MEDIUM,
        "scheduleWork" to RiskLevel.MEDIUM
    )

    override fun handle(command: RuntimeCommand): RuntimeResult {
        return RuntimeResult.ok("scheduling action accepted: ${command.action}")
    }
}

