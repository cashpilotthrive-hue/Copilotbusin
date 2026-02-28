package org.jetbrains.plugins.template.agent

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

@Service(Service.Level.PROJECT)
class CopilotBusinessAgentService(private val project: Project) {

    private val logger = thisLogger()
    private val tasks = CopyOnWriteArrayList<AgentTask>()

    var isRunning: Boolean = false
        private set

    val taskList: List<AgentTask>
        get() = tasks.toList()

    @Synchronized
    fun start() {
        if (isRunning) return
        isRunning = true
        logger.info("CopilotBusinessAgent started for project: ${project.name}")
        analyzeProject()
    }

    @Synchronized
    fun stop() {
        isRunning = false
        logger.info("CopilotBusinessAgent stopped for project: ${project.name}")
    }

    @Synchronized
    fun addTask(description: String): AgentTask {
        val task = AgentTask(UUID.randomUUID().toString(), description)
        tasks.add(task)
        if (isRunning) {
            processTask(task)
        }
        return task
    }

    fun clearTasks() {
        tasks.clear()
    }

    private fun analyzeProject() {
        val task = AgentTask(UUID.randomUUID().toString(), "Analyze project: ${project.name}")
        tasks.add(task)
        processTask(task)
    }

    private fun processTask(task: AgentTask) {
        task.status = AgentTaskStatus.IN_PROGRESS
        try {
            task.result = "Processed: ${task.description}"
            task.status = AgentTaskStatus.COMPLETED
            logger.info("Task completed: ${task.description}")
        } catch (e: Exception) {
            task.result = "Failed: ${e.message}"
            task.status = AgentTaskStatus.FAILED
            logger.error("Task failed: ${task.description}", e)
        }
    }
}
