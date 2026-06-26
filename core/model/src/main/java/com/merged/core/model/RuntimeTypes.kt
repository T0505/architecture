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

data class ModuleStatus(
    val id: String,
    val name: String,
    val state: String = "registered",
    val detail: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class AgentStatus(
    val appId: String = "",
    val backendUrl: String = "",
    val deviceId: String = "",
    val token: String = "",
    val registrationState: String = "pending",
    val configVersion: String = "unavailable",
    val updateState: String = "idle",
    val taskStatus: String = "idle",
    val lastSyncAt: Long? = null,
    val lastHeartbeatAt: Long? = null,
    val lastLogUploadAt: Long? = null,
    val lastUpdateCheckAt: Long? = null
)

data class RuntimeLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: String = "INFO",
    val tag: String,
    val message: String
)

data class RuntimeSnapshot(
    val deviceId: String = "",
    val deviceLabel: String = "",
    val serviceState: String = "stopped",
    val serviceMessage: String = "Runtime service is not running",
    val moduleStatuses: List<ModuleStatus> = emptyList(),
    val permissionStates: Map<String, String> = emptyMap(),
    val agentStatus: AgentStatus = AgentStatus(),
    val logs: List<RuntimeLogEntry> = emptyList()
)
