package plugins.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.Project
import plugins.editor.LineNumberManager
import plugins.settings.LineNumberSettings

@Service(Level.PROJECT)
class LineNumberService(project: Project) : Disposable {
    private val settings: LineNumberSettings = LineNumberSettings.getInstance()

    private val manager: LineNumberManager =
        ApplicationManager.getApplication().getService(LineNumberManager::class.java)

    private val editorFactoryListener: EditorFactoryListenerImpl = EditorFactoryListenerImpl(manager)

    init {
        EditorFactory.getInstance().addEditorFactoryListener(editorFactoryListener, this)
        manager.initialize()
    }

    fun getManager(): LineNumberManager = manager

    fun refreshAll() {
        manager.refreshAll()
    }

    override fun dispose() {}

    companion object {
        fun getInstance(project: Project): LineNumberService {
            return project.getService(LineNumberService::class.java)
        }
    }

    private class EditorFactoryListenerImpl(private val manager: LineNumberManager) :
        EditorFactoryListener {

        override fun editorCreated(event: EditorFactoryEvent) {
            manager.attachToEditor(event.editor)
        }

        override fun editorReleased(event: EditorFactoryEvent) {
            manager.detachFromEditor(event.editor)
        }
    }
}