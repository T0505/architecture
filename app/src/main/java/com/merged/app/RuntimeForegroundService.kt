package com.merged.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.merged.core.model.RuntimeCommand

class RuntimeForegroundService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val heartbeatTask = object : Runnable {
        override fun run() {
            val app = application as MergedRuntimeApp
            app.router.dispatch(
                RuntimeCommand(
                    target = "agent",
                    action = "heartbeat",
                    source = "runtime-service"
                )
            )
            app.router.dispatch(
                RuntimeCommand(
                    target = "agent",
                    action = "queryStatus",
                    source = "runtime-service"
                )
            )
            handler.postDelayed(this, HEARTBEAT_INTERVAL_MS)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopRuntime()
            ACTION_REFRESH -> runBootstrapTasks()
            else -> startRuntime()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        val app = application as MergedRuntimeApp
        app.stateStore.setServiceState("stopped", "Runtime service stopped")
        app.stateStore.appendLog("RuntimeService", "Runtime service stopped")
        super.onDestroy()
    }

    private fun startRuntime() {
        val app = application as MergedRuntimeApp
        app.stateStore.setServiceState("starting", "Preparing runtime modules")
        app.stateStore.appendLog("RuntimeService", "Starting runtime foreground service")
        try {
            startForeground(NOTIFICATION_ID, buildNotification("Runtime is starting"))
        } catch (error: RuntimeException) {
            app.stateStore.setServiceState("error", "Foreground start denied: ${error.javaClass.simpleName}")
            app.stateStore.appendLog(
                "RuntimeService",
                "Foreground start failed: ${error.message}",
                level = "ERROR"
            )
            stopSelf()
            return
        }
        runBootstrapTasks()
        handler.removeCallbacks(heartbeatTask)
        handler.postDelayed(heartbeatTask, HEARTBEAT_INTERVAL_MS)
    }

    private fun runBootstrapTasks() {
        val app = application as MergedRuntimeApp
        val commands = listOf(
            RuntimeCommand(target = "storage", action = "prepare", source = "runtime-service"),
            RuntimeCommand(target = "agent", action = "registerDevice", source = "runtime-service"),
            RuntimeCommand(target = "agent", action = "syncConfig", source = "runtime-service"),
            RuntimeCommand(target = "agent", action = "checkUpdate", source = "runtime-service"),
            RuntimeCommand(target = "launcher", action = "queryStatus", source = "runtime-service")
        )
        commands.forEach(app.router::dispatch)
        app.stateStore.setServiceState("running", "Runtime service is active")
        app.stateStore.appendLog("RuntimeService", "Runtime bootstrap tasks completed")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification("Runtime is running"))
    }

    private fun stopRuntime() {
        handler.removeCallbacksAndMessages(null)
        val app = application as MergedRuntimeApp
        app.stateStore.appendLog("RuntimeService", "Stopping runtime service")
        app.stateStore.setServiceState("stopping", "Stopping runtime service")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun buildNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Merged Runtime")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Merged Runtime",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Runtime service status and task notifications"
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_START = "com.merged.app.action.START_RUNTIME"
        const val ACTION_STOP = "com.merged.app.action.STOP_RUNTIME"
        const val ACTION_REFRESH = "com.merged.app.action.REFRESH_RUNTIME"

        private const val NOTIFICATION_CHANNEL_ID = "merged_runtime"
        private const val NOTIFICATION_ID = 1001
        private const val HEARTBEAT_INTERVAL_MS = 30_000L

        fun start(context: Context): Boolean {
            val app = context.applicationContext as? MergedRuntimeApp
            return try {
                val intent = Intent(context, RuntimeForegroundService::class.java).apply {
                    action = ACTION_START
                }
                ContextCompat.startForegroundService(context, intent)
                app?.stateStore?.appendLog("RuntimeService", "Foreground service launch intent sent from UI")
                true
            } catch (error: Throwable) {
                app?.stateStore?.setServiceState("error", "Launch request failed: ${error.javaClass.simpleName}")
                app?.stateStore?.appendLog(
                    "RuntimeService",
                    "Foreground service launch failed before start: ${error.message}",
                    level = "ERROR"
                )
                false
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, RuntimeForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
