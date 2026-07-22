package plugins.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import plugins.settings.LineNumberSettings

class ToggleLineNumberAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val settings = LineNumberSettings.getInstance()

        settings.isEnabled = !settings.isEnabled

        val message = if (settings.isEnabled) {
            "Line Numbers Enabled"
        } else {
            "Line Numbers Disabled"
        }

        Messages.showInfoMessage(project, message, "Line Numbers")
    }

    override fun update(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT)
        e.presentation.isEnabledAndVisible = project != null
    }
}