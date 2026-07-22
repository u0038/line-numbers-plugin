package plugins.utils

object LineNumberUtils {
    fun calculateRelativeNumber(currentLine: Int, targetLine: Int): Int {
        return targetLine - currentLine
    }

    fun formatLineNumber(absoluteLine: Int, relativeNumber: Int, showAbsolute: Boolean, showRelative: Boolean, absoluteFirst: Boolean): String {
        val parts = mutableListOf<String>()

        if (showAbsolute && showRelative) {
            if (absoluteFirst) {
                parts.add(absoluteLine.toString())
                parts.add(relativeNumber.toString())
            } else {
                parts.add(relativeNumber.toString())
                parts.add(absoluteLine.toString())
            }
        } else if (showAbsolute) {
            parts.add(absoluteLine.toString())
        } else if (showRelative) {
            parts.add(relativeNumber.toString())
        }

        return parts.joinToString(" ").trim()
    }
}