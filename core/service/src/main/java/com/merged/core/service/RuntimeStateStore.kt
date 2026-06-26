package com.merged.core.service

import com.merged.core.ipc.AuditRecord
import com.merged.core.ipc.AuditSink
import com.merged.core.model.AgentStatus
import com.merged.core.model.ModuleStatus
import com.merged.core.model.RuntimeLogEntry
import com.merged.core.model.RuntimeSnapshot
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RuntimeStateStore(
    private val runtimeLogFile: File
) : AuditSink {
    private val moduleStatuses = linkedMapOf<String, ModuleStatus>()
    private val permissionStates = linkedMapOf<String, String>()
    private val inMemoryLogs = ArrayDeque<RuntimeLogEntry>()
    private val logFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private var deviceId: String = ""
    private var deviceLabel: String = ""
    private var serviceState: String = "stopped"
    private var serviceMessage: String = "Runtime service is not running"
    private var agentStatus: AgentStatus = AgentStatus()

    @Synchronized
    fun setDeviceInfo(deviceId: String, deviceLabel: String) {
        this.deviceId = deviceId
        this.deviceLabel = deviceLabel
    }

    @Synchronized
    fun registerModules(modules: List<RuntimeModule>) {
        modules.forEach { module ->
            val current = moduleStatuses[module.capability.id]
            if (current == null) {
                moduleStatuses[module.capability.id] = ModuleStatus(
                    id = module.capability.id,
                    name = module.capability.name
                )
            }
        }
    }

    @Synchronized
    fun updateModuleState(id: String, name: String? = null, state: String, detail: String = "") {
        val previous = moduleStatuses[id]
        moduleStatuses[id] = ModuleStatus(
            id = id,
            name = name ?: previous?.name ?: id,
            state = state,
            detail = detail,
            updatedAt = System.currentTimeMillis()
        )
    }

    @Synchronized
    fun setPermissionState(permission: String, state: String) {
        permissionStates[permission] = state
    }

    @Synchronized
    fun setServiceState(state: String, message: String) {
        serviceState = state
        serviceMessage = message
    }

    @Synchronized
    fun updateAgentStatus(transform: (AgentStatus) -> AgentStatus) {
        agentStatus = transform(agentStatus)
    }

    @Synchronized
    fun appendLog(tag: String, message: String, level: String = "INFO") {
        val entry = RuntimeLogEntry(level = level, tag = tag, message = message)
        if (inMemoryLogs.size >= 200) {
            inMemoryLogs.removeFirst()
        }
        inMemoryLogs.addLast(entry)

        runtimeLogFile.parentFile?.mkdirs()
        runtimeLogFile.appendText(
            "${formatTimestamp(entry.timestamp)} ${entry.level}/${entry.tag}: ${entry.message}\n"
        )
    }

    @Synchronized
    fun snapshot(): RuntimeSnapshot {
        return RuntimeSnapshot(
            deviceId = deviceId,
            deviceLabel = deviceLabel,
            serviceState = serviceState,
            serviceMessage = serviceMessage,
            moduleStatuses = moduleStatuses.values.toList(),
            permissionStates = permissionStates.toMap(),
            agentStatus = agentStatus,
            logs = inMemoryLogs.toList()
        )
    }

    @Synchronized
    fun readLogText(maxLines: Int = 120): String {
        if (!runtimeLogFile.exists()) {
            return ""
        }
        return runtimeLogFile.readLines().takeLast(maxLines).joinToString(separator = "\n")
    }

    override fun record(record: AuditRecord) {
        val level = if (record.ok) "DEBUG" else "WARN"
        appendLog(
            tag = "CommandRouter",
            level = level,
            message = "[${record.requestId}] ${record.target}.${record.action} from ${record.source} -> ${record.message} (${record.durationMs}ms)"
        )
    }

    @Synchronized
    private fun formatTimestamp(timestamp: Long): String {
        return logFormatter.format(Date(timestamp))
    }
}
