package com.merged.app

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.merged.core.model.RuntimeCommand

class EngineRuntimeService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        (application as MergedRuntimeApp).router.dispatch(
            RuntimeCommand(target = "native", action = "startEngine", source = "engine-service")
        )
        return START_STICKY
    }

    override fun onDestroy() {
        (application as MergedRuntimeApp).router.dispatch(
            RuntimeCommand(target = "native", action = "stopEngine", source = "engine-service")
        )
        super.onDestroy()
    }
}

class AutomationRuntimeService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        (application as MergedRuntimeApp).router.dispatch(
            RuntimeCommand(target = "automation", action = "queryNode", source = "automation-service")
        )
        return START_STICKY
    }
}

class PluginRuntimeService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        (application as MergedRuntimeApp).router.dispatch(
            RuntimeCommand(target = "plugin", action = "start", source = "plugin-service")
        )
        return START_STICKY
    }

    override fun onDestroy() {
        (application as MergedRuntimeApp).router.dispatch(
            RuntimeCommand(target = "plugin", action = "stop", source = "plugin-service")
        )
        super.onDestroy()
    }
}
