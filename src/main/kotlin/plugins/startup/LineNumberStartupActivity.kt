package plugins.startup

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import plugins.service.LineNumberService
import plugins.settings.LineNumberSettings

class LineNumberStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val settings = LineNumberSettings.getInstance()

        val service = project.service<LineNumberService>()

        if (settings.isEnabled) {
            service.getManager()
        }
    }
}