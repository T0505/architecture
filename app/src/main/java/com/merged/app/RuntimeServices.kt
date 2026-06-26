package com.merged.app

import android.app.Service
import android.content.Intent
import android.os.IBinder

class EngineRuntimeService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}

class AutomationRuntimeService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}

class PluginRuntimeService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}

