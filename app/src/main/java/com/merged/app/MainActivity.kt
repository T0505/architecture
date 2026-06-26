package com.merged.app

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.merged.core.model.RuntimeCommand
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val uiHandler = Handler(Looper.getMainLooper())
    private val refreshTicker = object : Runnable {
        override fun run() {
            refreshUi()
            uiHandler.postDelayed(this, 1000L)
        }
    }

    private lateinit var headerView: TextView
    private lateinit var subtitleView: TextView
    private lateinit var serviceChipView: TextView
    private lateinit var deviceStatusView: TextView
    private lateinit var permissionStatusView: TextView
    private lateinit var agentStatusView: TextView
    private lateinit var moduleStatusView: TextView
    private lateinit var logView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MergedRuntimeApp
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(28))
        }

        headerView = titleTextView()
        subtitleView = subtitleTextView()
        serviceChipView = statusChip()
        deviceStatusView = sectionBodyTextView()
        permissionStatusView = sectionBodyTextView()
        agentStatusView = sectionBodyTextView()
        moduleStatusView = sectionBodyTextView().apply {
            typeface = Typeface.MONOSPACE
        }
        logView = sectionBodyTextView().apply {
            movementMethod = ScrollingMovementMethod()
            minLines = 12
            typeface = Typeface.MONOSPACE
            setTextColor(COLOR_LOG_TEXT)
        }

        content.addView(heroCard())
        content.addView(infoCard("设备状态", "Device + service health", deviceStatusView, CARD_DEVICE_START, CARD_DEVICE_END))
        content.addView(infoCard("权限状态", "What still needs attention", permissionStatusView, CARD_PERMISSION_START, CARD_PERMISSION_END))
        content.addView(controlCard(
            onStartClick = {
                app.router.dispatch(RuntimeCommand(target = "launcher", action = "startService", source = "ui"))
                val started = RuntimeForegroundService.start(this)
                toast(if (started) "已发送启动请求" else "启动请求失败，请看运行日志")
                refreshUi()
            },
            onStopClick = {
                app.router.dispatch(RuntimeCommand(target = "launcher", action = "stopService", source = "ui"))
                RuntimeForegroundService.stop(this)
                toast("已发送停止请求")
                refreshUi()
            },
            onLogsClick = {
                app.router.dispatch(RuntimeCommand(target = "agent", action = "uploadLog", source = "ui"))
                toast("已刷新日志视图")
                refreshUi(showLogsOnly = true)
            }
        ))
        content.addView(infoCard("Agent 状态", "Registration, config, heartbeat", agentStatusView, CARD_AGENT_START, CARD_AGENT_END))
        content.addView(infoCard("模块状态", "Runtime module registry snapshot", moduleStatusView, CARD_MODULE_START, CARD_MODULE_END))
        content.addView(infoCard("运行日志", "Latest runtime and command bus events", logView, CARD_LOG_START, CARD_LOG_END))

        setContentView(
            ScrollView(this).apply {
                isFillViewport = true
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(COLOR_BACKGROUND_TOP, COLOR_BACKGROUND_BOTTOM)
                )
                addView(
                    content,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        )

        app.router.dispatch(RuntimeCommand(target = "launcher", action = "refresh", source = "ui"))
        app.router.dispatch(RuntimeCommand(target = "agent", action = "queryStatus", source = "ui"))
        requestNotificationPermissionIfNeeded()
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        uiHandler.removeCallbacks(refreshTicker)
        uiHandler.post(refreshTicker)
        refreshUi()
    }

    override fun onPause() {
        uiHandler.removeCallbacks(refreshTicker)
        super.onPause()
    }

    private fun refreshUi(showLogsOnly: Boolean = false) {
        val app = application as MergedRuntimeApp
        app.runtimeManager.refreshPermissions()
        val snapshot = app.runtimeManager.snapshot()
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.US)

        headerView.text = "Merged Runtime"
        subtitleView.text = "主链路控制台"
        serviceChipView.text = snapshot.serviceState.uppercase(Locale.US)
        serviceChipView.background = pillDrawable(
            when (snapshot.serviceState) {
                "running" -> COLOR_SUCCESS
                "starting", "start_requested", "stop_requested", "stopping" -> COLOR_WARNING
                "error" -> COLOR_DANGER
                else -> COLOR_NEUTRAL
            }
        )

        deviceStatusView.text = buildString {
            appendLine("设备: ${snapshot.deviceLabel.ifBlank { "Unknown device" }}")
            appendLine("deviceId: ${snapshot.deviceId}")
            append("服务: ${snapshot.serviceState} / ${snapshot.serviceMessage}")
        }

        permissionStatusView.text = buildString {
            snapshot.permissionStates.forEach { (name, state) ->
                appendLine("$name: $state")
            }
        }

        agentStatusView.visibility = if (showLogsOnly) View.GONE else View.VISIBLE
        moduleStatusView.visibility = if (showLogsOnly) View.GONE else View.VISIBLE

        agentStatusView.text = buildString {
            appendLine("APPID: ${snapshot.agentStatus.appId.ifBlank { "-" }}")
            appendLine("后端: ${snapshot.agentStatus.backendUrl.ifBlank { "-" }}")
            appendLine("Token: ${snapshot.agentStatus.token.ifBlank { "-" }}")
            appendLine("注册: ${snapshot.agentStatus.registrationState}")
            appendLine("任务: ${snapshot.agentStatus.taskStatus}")
            appendLine("配置版本: ${snapshot.agentStatus.configVersion}")
            appendLine("更新状态: ${snapshot.agentStatus.updateState}")
            appendLine("心跳: ${formatTime(snapshot.agentStatus.lastHeartbeatAt, formatter)}")
            appendLine("拉配置: ${formatTime(snapshot.agentStatus.lastSyncAt, formatter)}")
            appendLine("日志上传: ${formatTime(snapshot.agentStatus.lastLogUploadAt, formatter)}")
            append("检查更新: ${formatTime(snapshot.agentStatus.lastUpdateCheckAt, formatter)}")
        }

        moduleStatusView.text = buildString {
            snapshot.moduleStatuses.forEach { module ->
                appendLine("${module.id}: ${module.state} ${module.detail}".trim())
            }
        }

        val logText = app.runtimeManager.readLogs().ifBlank { "暂无日志" }
        logView.text = logText
    }

    private fun heroCard(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = cardDrawable(CARD_HERO_START, CARD_HERO_END)
            setPadding(dp(20), dp(20), dp(20), dp(20))
            elevation = dp(4).toFloat()
            layoutParams = cardLayoutParams()

            addView(headerView)
            addView(subtitleView)
            addView(
                LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, dp(16), 0, 0)
                    addView(serviceChipView)
                    addView(metricBlock("Command Bus", "RuntimeCommand only"))
                    addView(metricBlock("Main Path", "Launcher + Agent + Logs"))
                }
            )
        }
    }

    private fun infoCard(
        title: String,
        subtitle: String,
        bodyView: TextView,
        startColor: Int,
        endColor: Int
    ): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = cardDrawable(startColor, endColor)
            setPadding(dp(18), dp(18), dp(18), dp(18))
            elevation = dp(2).toFloat()
            layoutParams = cardLayoutParams()
            addView(cardTitle(title))
            addView(cardSubtitle(subtitle))
            addView(spacer(12))
            addView(bodyView)
        }
    }

    private fun controlCard(
        onStartClick: () -> Unit,
        onStopClick: () -> Unit,
        onLogsClick: () -> Unit
    ): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = cardDrawable(CARD_CONTROL_START, CARD_CONTROL_END)
            setPadding(dp(18), dp(18), dp(18), dp(18))
            elevation = dp(2).toFloat()
            layoutParams = cardLayoutParams()
            addView(cardTitle("控制面板"))
            addView(cardSubtitle("Foreground service and runtime visibility"))
            addView(spacer(14))
            addView(
                LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(actionButton("启动服务", COLOR_TEAL, onStartClick))
                    addView(spacer(10))
                    addView(actionButton("停止服务", COLOR_CORAL, onStopClick))
                    addView(spacer(10))
                    addView(actionButton("查看日志", COLOR_INK, onLogsClick))
                }
            )
        }
    }

    private fun titleTextView(): TextView {
        return TextView(this).apply {
            textSize = 28f
            typeface = Typeface.create("serif-monospace", Typeface.BOLD)
            setTextColor(COLOR_TEXT_PRIMARY)
        }
    }

    private fun subtitleTextView(): TextView {
        return TextView(this).apply {
            textSize = 14f
            setTextColor(COLOR_TEXT_SECONDARY)
            setPadding(0, dp(6), 0, 0)
        }
    }

    private fun statusChip(): TextView {
        return TextView(this).apply {
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        }
    }

    private fun sectionBodyTextView(): TextView {
        return TextView(this).apply {
            textSize = 14f
            setTextColor(COLOR_TEXT_PRIMARY)
            setLineSpacing(0f, 1.2f)
        }
    }

    private fun cardTitle(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(COLOR_TEXT_PRIMARY)
        }
    }

    private fun cardSubtitle(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(COLOR_TEXT_SECONDARY)
            setPadding(0, dp(4), 0, 0)
        }
    }

    private fun metricBlock(label: String, value: String): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = panelDrawable()
            setPadding(dp(12), dp(10), dp(12), dp(10))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                leftMargin = dp(10)
            }
            addView(
                TextView(this@MainActivity).apply {
                    text = label
                    textSize = 11f
                    setTextColor(COLOR_TEXT_MUTED)
                }
            )
            addView(
                TextView(this@MainActivity).apply {
                    text = value
                    textSize = 13f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(COLOR_TEXT_PRIMARY)
                    setPadding(0, dp(4), 0, 0)
                }
            )
        }
    }

    private fun actionButton(label: String, backgroundColor: Int, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 15f
            isAllCaps = false
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = pillDrawable(backgroundColor, cornerRadius = dp(18).toFloat())
            setPadding(dp(18), dp(14), dp(18), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { onClick() }
        }
    }

    private fun formatTime(timestamp: Long?, formatter: SimpleDateFormat): String {
        return timestamp?.let { formatter.format(Date(it)) } ?: "-"
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return
        }
        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATIONS)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun cardDrawable(startColor: Int, endColor: Int): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(startColor, endColor)
        ).apply {
            cornerRadius = dp(24).toFloat()
            setStroke(dp(1), COLOR_CARD_STROKE)
        }
    }

    private fun panelDrawable(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(18).toFloat()
            setColor(COLOR_PANEL)
        }
    }

    private fun pillDrawable(color: Int, cornerRadius: Float = dp(999).toFloat()): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            this.cornerRadius = cornerRadius
            setColor(color)
        }
    }

    private fun cardLayoutParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(16)
        }
    }

    private fun spacer(heightDp: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(heightDp)
            )
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val REQUEST_NOTIFICATIONS = 1001
        private val COLOR_BACKGROUND_TOP = Color.parseColor("#F5E9D6")
        private val COLOR_BACKGROUND_BOTTOM = Color.parseColor("#DCE8E4")
        private val COLOR_TEXT_PRIMARY = Color.parseColor("#132321")
        private val COLOR_TEXT_SECONDARY = Color.parseColor("#50635F")
        private val COLOR_TEXT_MUTED = Color.parseColor("#6C7A76")
        private val COLOR_LOG_TEXT = Color.parseColor("#102422")
        private val COLOR_CARD_STROKE = Color.parseColor("#22FFFFFF")
        private val COLOR_PANEL = Color.parseColor("#30FFFFFF")
        private val COLOR_TEAL = Color.parseColor("#147A73")
        private val COLOR_CORAL = Color.parseColor("#C95F44")
        private val COLOR_INK = Color.parseColor("#29444D")
        private val COLOR_SUCCESS = Color.parseColor("#1C8A68")
        private val COLOR_WARNING = Color.parseColor("#D1912C")
        private val COLOR_DANGER = Color.parseColor("#B84C4C")
        private val COLOR_NEUTRAL = Color.parseColor("#586C68")
        private val CARD_HERO_START = Color.parseColor("#FFF4E6")
        private val CARD_HERO_END = Color.parseColor("#F4D8B5")
        private val CARD_DEVICE_START = Color.parseColor("#FDF8F1")
        private val CARD_DEVICE_END = Color.parseColor("#EBDCC8")
        private val CARD_PERMISSION_START = Color.parseColor("#ECF6F1")
        private val CARD_PERMISSION_END = Color.parseColor("#D7E8DE")
        private val CARD_CONTROL_START = Color.parseColor("#FFF7EC")
        private val CARD_CONTROL_END = Color.parseColor("#F0D5BA")
        private val CARD_AGENT_START = Color.parseColor("#F4F7F2")
        private val CARD_AGENT_END = Color.parseColor("#DFE6D8")
        private val CARD_MODULE_START = Color.parseColor("#F6F1EA")
        private val CARD_MODULE_END = Color.parseColor("#E3D8CC")
        private val CARD_LOG_START = Color.parseColor("#EAF3F1")
        private val CARD_LOG_END = Color.parseColor("#D6E4E1")
    }
}
