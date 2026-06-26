package com.merged.core.service

import com.merged.core.ipc.CommandHandler
import com.merged.core.ipc.CommandRouter
import com.merged.core.model.Capability
import com.merged.core.model.RuntimeResult

interface RuntimeModule : CommandHandler {
    val capability: Capability

    override val target: String
        get() = capability.id

    fun start(): RuntimeResult = RuntimeResult.ok("${capability.id} started")

    fun stop(): RuntimeResult = RuntimeResult.ok("${capability.id} stopped")
}

class ModuleRegistry {
    private val modules = linkedMapOf<String, RuntimeModule>()

    fun register(module: RuntimeModule) {
        modules[module.capability.id] = module
    }

    fun attachTo(router: CommandRouter) {
        modules.values.forEach(router::register)
    }

    fun modules(): List<RuntimeModule> = modules.values.toList()
}

