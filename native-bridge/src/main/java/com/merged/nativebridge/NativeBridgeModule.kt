package com.merged.nativebridge

import com.merged.core.model.Capability
import com.merged.core.model.RiskLevel
import com.merged.core.model.RuntimeCommand
import com.merged.core.model.RuntimeResult
import com.merged.core.service.RuntimeModule

class NativeBridgeModule : RuntimeModule {
    override val capability = Capability(
        id = "native",
        name = "JNI and native engine bridge",
        actions = setOf("load", "health", "startEngine", "stopEngine", "sendEvent")
    )

    override val riskByAction = mapOf(
        "load" to RiskLevel.MEDIUM,
        "health" to RiskLevel.LOW,
        "startEngine" to RiskLevel.MEDIUM,
        "stopEngine" to RiskLevel.MEDIUM,
        "sendEvent" to RiskLevel.MEDIUM
    )

    override fun handle(command: RuntimeCommand): RuntimeResult {
        return when (command.action) {
            "health" -> RuntimeResult.ok(data = mapOf("nativeLoaded" to "false"))
            "load" -> RuntimeResult.ok("native load requested")
            "startEngine" -> RuntimeResult.ok("native engine start requested")
            "stopEngine" -> RuntimeResult.ok("native engine stop requested")
            "sendEvent" -> RuntimeResult.ok("native event accepted")
            else -> RuntimeResult.error("unsupported native action: ${command.action}")
        }
    }
}

