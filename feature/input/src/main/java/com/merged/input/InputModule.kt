package com.merged.input

import com.merged.core.model.Capability
import com.merged.core.model.RiskLevel
import com.merged.core.model.RuntimeCommand
import com.merged.core.model.RuntimeResult
import com.merged.core.service.RuntimeModule

class InputModule : RuntimeModule {
    override val capability = Capability(
        id = "input",
        name = "Keyboard, text and touch input bridge",
        actions = setOf("sendKey", "sendText", "sendTouch")
    )

    override val riskByAction = mapOf(
        "sendKey" to RiskLevel.HIGH,
        "sendText" to RiskLevel.HIGH,
        "sendTouch" to RiskLevel.HIGH
    )

    override fun handle(command: RuntimeCommand): RuntimeResult {
        return when (command.action) {
            "sendKey" -> RuntimeResult.ok("key event requested")
            "sendText" -> RuntimeResult.ok("text input requested")
            "sendTouch" -> RuntimeResult.ok("touch event requested")
            else -> RuntimeResult.error("unsupported input action: ${command.action}")
        }
    }
}

