package com.merged.vision

import com.merged.core.model.Capability
import com.merged.core.model.RiskLevel
import com.merged.core.model.RuntimeCommand
import com.merged.core.model.RuntimeResult
import com.merged.core.service.RuntimeModule

class VisionModule : RuntimeModule {
    override val capability = Capability(
        id = "vision",
        name = "Capture, OCR and object detection",
        actions = setOf("capture", "ocr", "detect", "updateModel")
    )

    override val riskByAction = mapOf(
        "capture" to RiskLevel.HIGH,
        "ocr" to RiskLevel.MEDIUM,
        "detect" to RiskLevel.MEDIUM,
        "updateModel" to RiskLevel.MEDIUM
    )

    override fun handle(command: RuntimeCommand): RuntimeResult {
        return when (command.action) {
            "capture" -> RuntimeResult.ok("capture requested")
            "ocr" -> RuntimeResult.ok("ocr requested")
            "detect" -> RuntimeResult.ok("object detection requested")
            "updateModel" -> RuntimeResult.ok("model update requested")
            else -> RuntimeResult.error("unsupported vision action: ${command.action}")
        }
    }
}

