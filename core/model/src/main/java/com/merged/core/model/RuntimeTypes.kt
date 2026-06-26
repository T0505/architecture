package com.merged.core.model

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    RESTRICTED
}

data class Capability(
    val id: String,
    val name: String,
    val actions: Set<String>,
    val defaultRisk: RiskLevel = RiskLevel.MEDIUM
)

data class RuntimeCommand(
    val target: String,
    val action: String,
    val args: Map<String, String> = emptyMap(),
    val payload: String? = null,
    val requestId: String = System.currentTimeMillis().toString(),
    val source: String = "local"
)

data class RuntimeResult(
    val ok: Boolean,
    val message: String,
    val data: Map<String, String> = emptyMap()
) {
    companion object {
        fun ok(message: String = "ok", data: Map<String, String> = emptyMap()) =
            RuntimeResult(ok = true, message = message, data = data)

        fun error(message: String, data: Map<String, String> = emptyMap()) =
            RuntimeResult(ok = false, message = message, data = data)
    }
}

