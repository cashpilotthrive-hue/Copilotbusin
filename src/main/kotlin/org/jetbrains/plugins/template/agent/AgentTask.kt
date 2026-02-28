package org.jetbrains.plugins.template.agent

enum class AgentTaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

data class AgentTask(
    val id: String,
    val description: String,
    var status: AgentTaskStatus = AgentTaskStatus.PENDING,
    var result: String? = null
)
