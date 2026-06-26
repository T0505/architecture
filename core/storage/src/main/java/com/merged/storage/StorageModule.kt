package com.merged.storage

import com.merged.core.model.Capability
import com.merged.core.model.RiskLevel
import com.merged.core.model.RuntimeCommand
import com.merged.core.model.RuntimeResult
import com.merged.core.service.RuntimeModule
import java.io.File

class StorageModule(
    private val rootDir: File
) : RuntimeModule {
    override val capability = Capability(
        id = "storage",
        name = "File, script, model and log storage",
        actions = setOf("paths", "prepare"),
        defaultRisk = RiskLevel.LOW
    )

    override val riskByAction = mapOf(
        "paths" to RiskLevel.LOW,
        "prepare" to RiskLevel.MEDIUM
    )

    override fun handle(command: RuntimeCommand): RuntimeResult {
        return when (command.action) {
            "paths" -> RuntimeResult.ok(
                data = mapOf(
                    "scripts" to File(rootDir, "scripts").absolutePath,
                    "models" to File(rootDir, "models").absolutePath,
                    "plugins" to File(rootDir, "plugins").absolutePath,
                    "logs" to File(rootDir, "logs").absolutePath
                )
            )
            "prepare" -> {
                listOf("scripts", "models", "plugins", "logs").forEach {
                    File(rootDir, it).mkdirs()
                }
                RuntimeResult.ok("storage prepared")
            }
            else -> RuntimeResult.error("unsupported storage action: ${command.action}")
        }
    }
}

