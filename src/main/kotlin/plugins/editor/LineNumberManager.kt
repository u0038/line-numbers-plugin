package plugins.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.FoldRegion
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.FoldingListener
import com.intellij.openapi.editor.ex.FoldingModelEx
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.util.Key
import plugins.settings.LineNumberSettings
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.APP)
class LineNumberManager : CaretListener, DocumentListener, FoldingListener {
    private val settings: LineNumberSettings
        get() = LineNumberSettings.getInstance()

    private val editorRenderers = ConcurrentHashMap<Editor, LineHighlightersManager>()
    private var initialized = false

    @Synchronized
    fun initialize() {
        if (initialized) return
        initialized = true
        initializeExistingEditors()
    }

    private fun initializeExistingEditors() {
        EditorFactory.getInstance().allEditors.forEach { editor ->
            attachToEditor(editor)
        }
    }

    fun attachToEditor(editor: Editor) {
        if (editor.getUserData(MANAGER_KEY) != null) {
            return
        }
        if (editorRenderers.containsKey(editor)) {
            return
        }

        val highlighterManager = LineHighlightersManager(editor, settings)
        editorRenderers[editor] = highlighterManager
        editor.putUserData(MANAGER_KEY, highlighterManager)

        ApplicationManager.getApplication().invokeAndWait {
            editor.settings.isLineNumbersShown = !settings.isHideNativeLineNumbers
        }

        editor.caretModel.addCaretListener(this)
        editor.document.addDocumentListener(this)

        (editor.foldingModel as? FoldingModelEx)?.addListener(this, highlighterManager)

        updateCaretPosition(editor)
    }

    fun detachFromEditor(editor: Editor) {
        val highlighterManager = editorRenderers.remove(editor) ?: return

        editor.caretModel.removeCaretListener(this)
        editor.document.removeDocumentListener(this)

        editor.putUserData(MANAGER_KEY, null)

        ApplicationManager.getApplication().invokeAndWait {
            editor.settings.isLineNumbersShown = true
        }

        highlighterManager.dispose()
    }

    override fun caretAdded(event: CaretEvent) {
        updateCaretPosition(event.editor)
    }

    override fun caretRemoved(event: CaretEvent) {
        updateCaretPosition(event.editor)
    }

    override fun caretPositionChanged(event: CaretEvent) {
        updateCaretPosition(event.editor)
    }

    override fun documentChanged(event: DocumentEvent) {
        editorRenderers.keys.forEach { editor ->
            if (editor.document == event.document) {
                refreshEditor(editor)
            }
        }
    }

    override fun onFoldRegionStateChange(region: FoldRegion) {
        val editor = region.editor
        refreshEditor(editor)
    }

    override fun onFoldProcessingEnd() {
        editorRenderers.keys.forEach { editor ->
            refreshEditor(editor)
        }
    }

    private fun updateCaretPosition(editor: Editor) {
        val highlighterManager = editorRenderers[editor] ?: return
        val currentLine = editor.caretModel.logicalPosition.line
        highlighterManager.updateCaretPosition(currentLine)
    }

    private fun refreshEditor(editor: Editor) {
        val highlighterManager = editorRenderers[editor] ?: return
        highlighterManager.refresh()
    }

    fun refreshAll() {
        editorRenderers.keys.forEach { editor ->
            refreshEditor(editor)
        }
    }

    fun onSettingsChanged() {
        editorRenderers.keys.forEach { editor ->
            ApplicationManager.getApplication().invokeAndWait {
                editor.settings.isLineNumbersShown = !settings.isHideNativeLineNumbers
            }
            refreshEditor(editor)
        }
    }

    fun dispose() {
        editorRenderers.forEach { (editor, manager) ->
            editor.caretModel.removeCaretListener(this)
            editor.document.removeDocumentListener(this)
            editor.putUserData(MANAGER_KEY, null)
            manager.dispose()
        }

        editorRenderers.clear()
        initialized = false
    }

    private class LineHighlightersManager(
        private val editor: Editor,
        private val settings: LineNumberSettings
    ) : com.intellij.openapi.Disposable {
        private var currentLine: Int = -1
        private val highlighters = mutableListOf<RangeHighlighter>()

        fun updateCaretPosition(line: Int) {
            currentLine = line
            refresh()
        }

        fun refresh() {
            clearHighlighters()

            if (!settings.isEnabled) {
                return
            }

            val document = editor.document
            val lineCount = document.lineCount
            val markupModel = editor.markupModel

            for (line in 0 until lineCount) {
                try {
                    val startOffset = document.getLineStartOffset(line)
                    val endOffset = document.getLineEndOffset(line)

                    // Skip lines that are hidden in collapsed fold regions
                    val foldingModel = editor.foldingModel
                    val foldRegion = foldingModel.getCollapsedRegionAtOffset(startOffset)
                    if (foldRegion != null && startOffset >= foldRegion.startOffset && endOffset <= foldRegion.endOffset) {
                        // This entire line is inside a collapsed fold region - skip it
                        continue
                    }

                    val renderer = LineNumberRenderer(
                        editor = editor,
                        line = line,
                        currentLine = currentLine
                    )

                    val highlighter = markupModel.addRangeHighlighter(
                        startOffset,
                        endOffset,
                        3000,
                        null,
                        HighlighterTargetArea.EXACT_RANGE
                    )

                    highlighter.let {
                        it.gutterIconRenderer = renderer
                        highlighters.add(it)
                    }
                } catch (_: Exception) {
                    // The line may no longer be valid because the document changed.
                }
            }
        }

        private fun clearHighlighters() {
            highlighters.forEach { highlighter ->
                if (!highlighter.isValid) {
                    return@forEach
                }
                editor.markupModel.removeHighlighter(highlighter)
            }

            highlighters.clear()
        }

        override fun dispose() {
            clearHighlighters()
        }
    }

    companion object {
        private val MANAGER_KEY = Key.create<LineHighlightersManager>(
            "u0038.line-numbers.manager"
        )
    }
}