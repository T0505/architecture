package com.merged.app

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.merged.core.model.RuntimeResult
import com.merged.core.model.RuntimeSnapshot
import com.merged.core.service.RuntimeControl
import com.merged.core.service.RuntimeStateStore
import java.util.UUID
import java.io.File

class RuntimeManager(
    private val context: Context,
    val stateStore: RuntimeStateStore
) : RuntimeControl {
    private val prefs = context.getSharedPreferences("runtime_manager", Context.MODE_PRIVATE)

    init {
        ensureDeviceIdentity()
        refreshPermissions()
        stateStore.setServiceState("stopped", "Runtime service is not running")
        stateStore.updateAgentStatus { status ->
            status.copy(
                appId = prefs.getString(KEY_APP_ID, DEFAULT_APP_ID).orEmpty(),
                backendUrl = prefs.getString(KEY_BACKEND_URL, DEFAULT_BACKEND_URL).orEmpty(),
                token = prefs.getString(KEY_TOKEN, DEFAULT_TOKEN).orEmpty(),
                deviceId = prefs.getString(KEY_DEVICE_ID, "") ?: ""
            )
        }
    }

    override fun startRuntimeService(): RuntimeResult {
        refreshPermissions()
        stateStore.setServiceState("start_requested", "Waiting for a visible UI to launch the foreground service")
        stateStore.appendLog("RuntimeManager", "Foreground service start requested")
        return RuntimeResult.ok("runtime service start requested from launcher")
    }

    override fun stopRuntimeService(): RuntimeResult {
        stateStore.setServiceState("stop_requested", "Foreground service stop requested")
        stateStore.appendLog("RuntimeManager", "Foreground service stop requested")
        return RuntimeResult.ok("runtime service stop requested from launcher")
    }

    override fun refreshRuntimeStatus(): RuntimeResult {
        refreshPermissions()
        return RuntimeResult.ok(
            message = "runtime status refreshed",
            data = snapshotMap(stateStore.snapshot())
        )
    }

    fun snapshot(): RuntimeSnapshot = stateStore.snapshot()

    fun readLogs(maxLines: Int = 120): String = stateStore.readLogText(maxLines)

    fun refreshPermissions() {
        stateStore.setPermissionState("internet", "granted")
        stateStore.setPermissionState("foreground_service", "granted")
        stateStore.setPermissionState(
            "notifications",
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                "granted"
            } else {
                "required"
            }
        )
        stateStore.setPermissionState(
            "accessibility",
            if (isAccessibilityEnabled()) "not_connected" else "required"
        )
        stateStore.setPermissionState("overlay", if (Settings.canDrawOverlays(context)) "granted" else "optional")
    }

    private fun ensureDeviceIdentity() {
        val deviceId = prefs.getString(KEY_DEVICE_ID, null) ?: "device-${UUID.randomUUID()}"
        prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        stateStore.setDeviceInfo(
            deviceId = deviceId,
            deviceLabel = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
        )
    }

    private fun isAccessibilityEnabled(): Boolean {
        return try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED,
                0
            ) == 1
        } catch (_: Throwable) {
            false
        }
    }

    private fun snapshotMap(snapshot: RuntimeSnapshot): Map<String, String> {
        return mapOf(
            "deviceId" to snapshot.deviceId,
            "serviceState" to snapshot.serviceState,
            "serviceMessage" to snapshot.serviceMessage,
            "moduleCount" to snapshot.moduleStatuses.size.toString(),
            "logCount" to snapshot.logs.size.toString()
        )
    }

    companion object {
        private const val KEY_APP_ID = "app_id"
        private const val KEY_BACKEND_URL = "backend_url"
        private const val KEY_TOKEN = "token"
        private const val KEY_DEVICE_ID = "device_id"

        private const val DEFAULT_APP_ID = "demo-app"
        private const val DEFAULT_BACKEND_URL = "https://config.local/runtime"
        private const val DEFAULT_TOKEN = "demo-token"

        fun create(context: Context): RuntimeManager {
            val stateStore = RuntimeStateStore(File(context.filesDir, "logs/runtime.log"))
            return RuntimeManager(context.applicationContext, stateStore)
        }
    }
}
