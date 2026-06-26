package com.merged.app

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MergedRuntimeApp
        val moduleNames = app.registry.modules()
            .joinToString(separator = "\n") { "- ${it.capability.id}: ${it.capability.name}" }

        val textView = TextView(this).apply {
            text = "Merged Runtime\n\nRegistered modules:\n$moduleNames"
            textSize = 16f
            setPadding(32, 32, 32, 32)
        }

        setContentView(LinearLayout(this).apply { addView(textView) })
    }
}

