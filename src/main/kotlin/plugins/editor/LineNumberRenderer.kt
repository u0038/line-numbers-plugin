package plugins.editor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import plugins.settings.LineNumberSettings
import plugins.utils.LineNumberUtils
import java.awt.*
import javax.swing.Icon

class LineNumberRenderer(
    private val editor: Editor,
    private val line: Int,
    private val currentLine: Int
) : GutterIconRenderer() {
    private val settings: LineNumberSettings
        get() = LineNumberSettings.getInstance()
    private val relativeNumber: Int
        get() {
            val currentLine = editor.caretModel.logicalPosition.line
            
            val foldingModel = editor.foldingModel
            val lineOffset = editor.document.getLineStartOffset(line)
            val foldRegion = foldingModel.getCollapsedRegionAtOffset(lineOffset)

            val targetLine = if (foldRegion != null) {
                editor.document.getLineNumber(foldRegion.startOffset)
            } else {
                line
            }

            return LineNumberUtils.calculateRelativeNumber(
                currentLine,
                targetLine
            )
        }

    private val text: String = run {
        val mode = settings.lineNumberMode
        when (mode) {
            LineNumberSettings.LineNumberMode.ABSOLUTE -> {
                (line + 1).toString()
            }
            LineNumberSettings.LineNumberMode.RELATIVE -> {
                val rel = relativeNumber
                val absRel = if (rel < 0) -rel else rel
                absRel.toString()
            }
            LineNumberSettings.LineNumberMode.HYBRID -> {
                if (relativeNumber == 0) {
                    (line + 1).toString()
                } else {
                    val rel = relativeNumber
                    val absRel = if (rel < 0) -rel else rel
                    absRel.toString()
                }
            }
        }
    }

    override fun getIcon(): Icon {
        return RelativeNumberIcon(text, line == currentLine, editor)
    }

    override fun getAlignment(): Alignment = Alignment.LEFT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LineNumberRenderer) return false
        return line == other.line && currentLine == other.currentLine
    }

    override fun hashCode(): Int {
        var result = line
        result = 31 * result + currentLine
        return result
    }

    private class RelativeNumberIcon(
        private val text: String,
        private val isCurrentLine: Boolean,
        private val editor: Editor
    ) : Icon {

        companion object {
            private const val ICON_WIDTH = 32
            private const val RIGHT_PADDING = 8
        }

        override fun paintIcon(
            c: Component?,
            g: Graphics,
            x: Int,
            y: Int
        ) {
            val g2d = g.create() as Graphics2D

            try {
                g2d.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                )

                g2d.font = editor.colorsScheme.getFont(EditorFontType.PLAIN)

                val fontMetrics = g2d.fontMetrics

                val baseline = y + (editor.lineHeight - fontMetrics.height) / 2 + fontMetrics.ascent

                val defaultColor = editor.colorsScheme.getColor(EditorColors.LINE_NUMBERS_COLOR) ?: JBColor.GRAY
                val lineColor = if (isCurrentLine) {
                    try {
                        val instance = LineNumberSettings.getInstance()
                        val colorHex = instance.currentLineColor
                        if (colorHex != null) {
                            ColorUtil.fromHex(colorHex) ?: defaultColor
                        } else {
                            defaultColor
                        }
                    } catch (e: Exception) {
                        defaultColor
                    }
                } else {
                    defaultColor
                }

                g2d.color = lineColor

                val textWidth = fontMetrics.stringWidth(text)
                val drawX = x + ICON_WIDTH - textWidth - RIGHT_PADDING

                g2d.drawString(
                    text,
                    drawX,
                    baseline
                )
            } finally {
                g2d.dispose()
            }
        }

        override fun getIconWidth(): Int = ICON_WIDTH

        override fun getIconHeight(): Int = editor.lineHeight
    }
}
