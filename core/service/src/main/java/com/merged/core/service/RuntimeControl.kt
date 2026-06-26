package com.merged.core.service

import com.merged.core.model.RuntimeResult

interface RuntimeControl {
    fun startRuntimeService(): RuntimeResult

    fun stopRuntimeService(): RuntimeResult

    fun refreshRuntimeStatus(): RuntimeResult
}
