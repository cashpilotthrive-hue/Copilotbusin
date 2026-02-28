package org.jetbrains.plugins.template

import com.intellij.openapi.components.service
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.jetbrains.plugins.template.agent.AgentTaskStatus
import org.jetbrains.plugins.template.agent.CopilotBusinessAgentService

class CopilotAgentServiceTest : BasePlatformTestCase() {

    fun testAgentInitialState() {
        val agentService = project.service<CopilotBusinessAgentService>()
        assertFalse(agentService.isRunning)
        assertTrue(agentService.taskList.isEmpty())
    }

    fun testAgentStartCreatesAnalysisTask() {
        val agentService = project.service<CopilotBusinessAgentService>()
        agentService.start()

        assertTrue(agentService.isRunning)
        assertFalse(agentService.taskList.isEmpty())
        val firstTask = agentService.taskList.first()
        assertTrue(firstTask.description.contains("Analyze project"))
        assertEquals(AgentTaskStatus.COMPLETED, firstTask.status)

        agentService.stop()
    }

    fun testAgentStop() {
        val agentService = project.service<CopilotBusinessAgentService>()
        agentService.start()
        assertTrue(agentService.isRunning)

        agentService.stop()
        assertFalse(agentService.isRunning)
    }

    fun testAddTaskWhileRunning() {
        val agentService = project.service<CopilotBusinessAgentService>()
        agentService.start()

        val task = agentService.addTask("Review business logic")
        assertEquals("Review business logic", task.description)
        assertEquals(AgentTaskStatus.COMPLETED, task.status)
        assertNotNull(task.result)

        agentService.stop()
    }

    fun testAddTaskWhileStopped() {
        val agentService = project.service<CopilotBusinessAgentService>()
        val task = agentService.addTask("Pending analysis")
        assertEquals(AgentTaskStatus.PENDING, task.status)
    }

    fun testClearTasks() {
        val agentService = project.service<CopilotBusinessAgentService>()
        agentService.start()
        assertFalse(agentService.taskList.isEmpty())

        agentService.clearTasks()
        assertTrue(agentService.taskList.isEmpty())

        agentService.stop()
    }

    fun testStartIsIdempotent() {
        val agentService = project.service<CopilotBusinessAgentService>()
        agentService.start()
        val taskCountAfterFirstStart = agentService.taskList.size

        agentService.start()
        assertEquals(taskCountAfterFirstStart, agentService.taskList.size)

        agentService.stop()
    }
}
