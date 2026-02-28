package org.jetbrains.plugins.template.toolWindow

import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.content.ContentFactory
import org.jetbrains.plugins.template.MyBundle
import org.jetbrains.plugins.template.agent.AgentTask
import org.jetbrains.plugins.template.agent.AgentTaskStatus
import org.jetbrains.plugins.template.agent.CopilotBusinessAgentService
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField

class CopilotAgentToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val agentToolWindow = CopilotAgentToolWindow(project)
        val content = ContentFactory.getInstance().createContent(agentToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class CopilotAgentToolWindow(private val project: Project) {

        private val agentService = project.service<CopilotBusinessAgentService>()
        private val taskArea = JBTextArea(10, 40).apply { isEditable = false }
        private val statusLabel = JBLabel(MyBundle.message("agentStatus", MyBundle.message("agentStopped")))
        private val taskInput = JTextField(30)

        fun getContent(): JBPanel<*> {
            val panel = JBPanel<JBPanel<*>>(BorderLayout())

            val controlPanel = JPanel(FlowLayout(FlowLayout.LEFT))
            val startButton = JButton(MyBundle.message("agentStart")).apply {
                addActionListener {
                    agentService.start()
                    refreshView()
                }
            }
            val stopButton = JButton(MyBundle.message("agentStop")).apply {
                addActionListener {
                    agentService.stop()
                    refreshView()
                }
            }
            val clearButton = JButton(MyBundle.message("agentClear")).apply {
                addActionListener {
                    agentService.clearTasks()
                    refreshView()
                }
            }
            controlPanel.add(startButton)
            controlPanel.add(stopButton)
            controlPanel.add(clearButton)

            val inputPanel = JPanel(FlowLayout(FlowLayout.LEFT))
            val addTaskButton = JButton(MyBundle.message("agentAddTask")).apply {
                addActionListener {
                    val description = taskInput.text.trim()
                    if (description.isNotEmpty()) {
                        agentService.addTask(description)
                        taskInput.text = ""
                        refreshView()
                    }
                }
            }
            inputPanel.add(JBLabel(MyBundle.message("agentTaskLabel")))
            inputPanel.add(taskInput)
            inputPanel.add(addTaskButton)

            val topPanel = JPanel(BorderLayout())
            topPanel.add(statusLabel, BorderLayout.NORTH)
            topPanel.add(controlPanel, BorderLayout.CENTER)
            topPanel.add(inputPanel, BorderLayout.SOUTH)

            panel.add(topPanel, BorderLayout.NORTH)
            panel.add(JBScrollPane(taskArea), BorderLayout.CENTER)

            refreshView()
            return panel
        }

        private fun refreshView() {
            invokeLater {
                val running = agentService.isRunning
                val statusText = if (running) MyBundle.message("agentRunning") else MyBundle.message("agentStopped")
                statusLabel.text = MyBundle.message("agentStatus", statusText)
                taskArea.text = agentService.taskList.joinToString("\n") { formatTask(it) }
            }
        }

        private fun formatTask(task: AgentTask): String {
            val icon = when (task.status) {
                AgentTaskStatus.PENDING -> "[ ]"
                AgentTaskStatus.IN_PROGRESS -> "[~]"
                AgentTaskStatus.COMPLETED -> "[✓]"
                AgentTaskStatus.FAILED -> "[✗]"
            }
            return "$icon ${task.description}${task.result?.let { " → $it" } ?: ""}"
        }
    }
}
