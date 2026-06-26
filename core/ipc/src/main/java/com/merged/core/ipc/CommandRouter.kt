package com.merged.core.ipc

import com.merged.core.model.RiskLevel
import com.merged.core.model.RuntimeCommand
import com.merged.core.model.RuntimeResult

interface CommandHandler {
    val target: String
    val riskByAction: Map<String, RiskLevel>
    fun handle(command: RuntimeCommand): RuntimeResult
}

sealed class PolicyDecision {
    data object Allowed : PolicyDecision()
    data class Denied(val reason: String) : PolicyDecision()
}

interface CommandPolicy {
    fun check(command: RuntimeCommand, handler: CommandHandler): PolicyDecision
}

class DefaultCommandPolicy(
    private val highRiskAllowList: Set<String> = emptySet()
) : CommandPolicy {
    override fun check(command: RuntimeCommand, handler: CommandHandler): PolicyDecision {
        val risk = handler.riskByAction[command.action] ?: RiskLevel.MEDIUM
        val actionKey = "${command.target}.${command.action}"

        return when (risk) {
            RiskLevel.LOW, RiskLevel.MEDIUM -> PolicyDecision.Allowed
            RiskLevel.HIGH -> {
                val userGranted = command.args["userGranted"] == "true"
                if (userGranted && actionKey in highRiskAllowList) {
                    PolicyDecision.Allowed
                } else {
                    PolicyDecision.Denied("high risk command requires explicit allow-list and user grant")
                }
            }
            RiskLevel.RESTRICTED -> PolicyDecision.Denied("restricted command is not enabled in this architecture")
        }
    }
}

class CommandRouter(
    private val policy: CommandPolicy
) {
    private val handlers = linkedMapOf<String, CommandHandler>()

    fun register(handler: CommandHandler) {
        require(handler.target.isNotBlank()) { "handler target is blank" }
        handlers[handler.target] = handler
    }

    fun dispatch(command: RuntimeCommand): RuntimeResult {
        val handler = handlers[command.target]
            ?: return RuntimeResult.error("unknown target: ${command.target}")

        return when (val decision = policy.check(command, handler)) {
            PolicyDecision.Allowed -> handler.handle(command)
            is PolicyDecision.Denied -> RuntimeResult.error(decision.reason)
        }
    }
}

