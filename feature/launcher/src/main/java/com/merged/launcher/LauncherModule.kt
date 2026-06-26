package com.merged.launcher

import com.merged.core.model.Capability
import com.merged.core.model.RiskLevel
import com.merged.core.model.RuntimeCommand
import com.merged.core.model.RuntimeResult
import com.merged.core.service.ModuleRegistry
import com.merged.core.service.RuntimeControl
import com.merged.core.service.RuntimeModule
import com.merged.core.service.RuntimeStateStore

class LauncherModule(
    private val runtimeControl: RuntimeControl,
    private val stateStore: RuntimeStateStore,
    private val registry: ModuleRegistry
) : RuntimeModule {
    override val capability = Capability(
        id = "launcher",
        name = "Launcher and status panel",
        actions = setOf("queryStatus", "openPanel", "startService", "stopService", "viewLogs", "refresh")
    )

    override val riskByAction = mapOf(
        "queryStatus" to RiskLevel.LOW,
        "openPanel" to RiskLevel.LOW,
        "startService" to RiskLevel.MEDIUM,
        "stopService" to RiskLevel.MEDIUM,
        "viewLogs" to RiskLevel.LOW,
        "refresh" to RiskLevel.LOW
    )

    override fun handle(command: RuntimeCommand): RuntimeResult {
        return when (command.action) {
            "queryStatus", "refresh" -> {
                runtimeControl.refreshRuntimeStatus()
                val snapshot = stateStore.snapshot()
                RuntimeResult.ok(
                    message = "launcher status ready",
                    data = mapOf(
                        "deviceId" to snapshot.deviceId,
                        "serviceState" to snapshot.serviceState,
                        "serviceMessage" to snapshot.serviceMessage,
                        "moduleCount" to registry.modules().size.toString(),
                        "agentTaskStatus" to snapshot.agentStatus.taskStatus
                    )
                )
            }
            "startService" -> runtimeControl.startRuntimeService()
            "stopService" -> runtimeControl.stopRuntimeService()
            "viewLogs" -> RuntimeResult.ok(
                "runtime logs ready",
                data = mapOf("logs" to stateStore.readLogText())
            )
            "openPanel" -> RuntimeResult.ok("launcher panel requested")
            else -> RuntimeResult.error("unsupported launcher action: ${command.action}")
        }
    }
}
