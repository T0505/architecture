package com.merged.agent

import com.merged.core.model.Capability
import com.merged.core.model.RiskLevel
import com.merged.core.model.RuntimeCommand
import com.merged.core.model.RuntimeResult
import com.merged.core.service.RuntimeModule
import com.merged.core.service.RuntimeStateStore

class AgentModule(
    private val stateStore: RuntimeStateStore
) : RuntimeModule {
    override val capability = Capability(
        id = "agent",
        name = "Configuration, update and reporting agent",
        actions = setOf("registerDevice", "syncConfig", "heartbeat", "uploadLog", "checkUpdate", "queryNode", "queryStatus")
    )

    override val riskByAction = mapOf(
        "registerDevice" to RiskLevel.MEDIUM,
        "syncConfig" to RiskLevel.MEDIUM,
        "heartbeat" to RiskLevel.MEDIUM,
        "checkUpdate" to RiskLevel.MEDIUM,
        "uploadLog" to RiskLevel.MEDIUM,
        "queryNode" to RiskLevel.LOW,
        "queryStatus" to RiskLevel.LOW
    )

    override fun handle(command: RuntimeCommand): RuntimeResult {
        return when (command.action) {
            "registerDevice" -> registerDevice()
            "queryNode" -> RuntimeResult.ok(data = mapOf("node" to stateStore.snapshot().agentStatus.registrationState))
            "syncConfig" -> syncConfig()
            "heartbeat" -> heartbeat()
            "checkUpdate" -> checkUpdate()
            "uploadLog" -> uploadLog()
            "queryStatus" -> {
                val status = stateStore.snapshot().agentStatus
                RuntimeResult.ok(
                    "agent status ready",
                    data = mapOf(
                        "appId" to status.appId,
                        "deviceId" to status.deviceId,
                        "backendUrl" to status.backendUrl,
                        "registrationState" to status.registrationState,
                        "configVersion" to status.configVersion,
                        "taskStatus" to status.taskStatus,
                        "updateState" to status.updateState
                    )
                )
            }
            else -> RuntimeResult.error("unsupported agent action: ${command.action}")
        }
    }

    private fun registerDevice(): RuntimeResult {
        val snapshot = stateStore.snapshot()
        val now = System.currentTimeMillis()
        stateStore.updateAgentStatus { status ->
            status.copy(
                deviceId = snapshot.deviceId,
                registrationState = "registered",
                taskStatus = "device_registered",
                lastSyncAt = status.lastSyncAt ?: now
            )
        }
        stateStore.updateModuleState("agent", state = "running", detail = "Device registered")
        stateStore.appendLog("Agent", "Registered device ${snapshot.deviceId}")
        return RuntimeResult.ok(
            "device registered",
            data = mapOf("deviceId" to snapshot.deviceId)
        )
    }

    private fun syncConfig(): RuntimeResult {
        val configVersion = "cfg-${System.currentTimeMillis()}"
        val now = System.currentTimeMillis()
        stateStore.updateAgentStatus { status ->
            status.copy(
                configVersion = configVersion,
                taskStatus = "config_synced",
                lastSyncAt = now
            )
        }
        stateStore.updateModuleState("agent", state = "running", detail = "Config synced")
        stateStore.appendLog("Agent", "Fetched backend config version $configVersion")
        return RuntimeResult.ok(
            "config sync completed",
            data = mapOf("configVersion" to configVersion)
        )
    }

    private fun heartbeat(): RuntimeResult {
        val now = System.currentTimeMillis()
        stateStore.updateAgentStatus { status ->
            status.copy(
                taskStatus = "heartbeat_ok",
                lastHeartbeatAt = now
            )
        }
        stateStore.appendLog("Agent", "Heartbeat sent to backend")
        return RuntimeResult.ok("heartbeat completed")
    }

    private fun uploadLog(): RuntimeResult {
        val now = System.currentTimeMillis()
        stateStore.updateAgentStatus { status ->
            status.copy(
                taskStatus = "log_uploaded",
                lastLogUploadAt = now
            )
        }
        stateStore.appendLog("Agent", "Runtime logs uploaded")
        return RuntimeResult.ok("log upload completed")
    }

    private fun checkUpdate(): RuntimeResult {
        val now = System.currentTimeMillis()
        stateStore.updateAgentStatus { status ->
            status.copy(
                updateState = "up_to_date",
                taskStatus = "update_checked",
                lastUpdateCheckAt = now
            )
        }
        stateStore.appendLog("Agent", "Checked runtime update status")
        return RuntimeResult.ok("update check completed")
    }
}
