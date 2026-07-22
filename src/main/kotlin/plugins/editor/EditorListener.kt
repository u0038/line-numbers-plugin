package plugins.editor

import com.intellij.openapi.editor.Editor

interface EditorListener {
    fun onEditorOpened(editor: Editor)

    fun onEditorClosed(editor: Editor)

    fun onCaretPositionChanged(editor: Editor, line: Int)
}