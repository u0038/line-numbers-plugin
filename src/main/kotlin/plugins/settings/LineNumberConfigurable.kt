package plugins.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.ColorPanel
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import plugins.editor.LineNumberManager
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel

class LineNumberConfigurable : Configurable {
    private val enabledCheckBox = JBCheckBox("Enable Line Numbers")
    private val hideNativeCheckBox = JBCheckBox("Hide IDE's built-in line numbers")

    // Line number mode selection
    private val modeComboBox = JComboBox(arrayOf("Relative", "Absolute", "Hybrid"))

    // Current line color components using standard ColorPanel
    private val useCustomColorCheckBox = JBCheckBox("Foreground")
    private val colorPanel = ColorPanel()

    private val mainPanel: JPanel by lazy {
        val panel = JPanel(VerticalFlowLayout(VerticalFlowLayout.TOP))
        panel.border = JBUI.Borders.empty(10)

        panel.add(JBLabel("Line Numbers Settings"))
        panel.add(enabledCheckBox)
        panel.add(hideNativeCheckBox)

        // Mode Selection Panel
        val modePanel = JPanel()
        modePanel.layout = java.awt.FlowLayout(java.awt.FlowLayout.LEFT)
        modePanel.add(JBLabel("Line Number Mode:"))
        modePanel.add(modeComboBox)
        panel.add(modePanel)

        // Current line color section
        panel.add(JBLabel(" "))
        panel.add(JBLabel("Current Line Highlight:"))

        val linePanel = JPanel()
        linePanel.layout = java.awt.FlowLayout(java.awt.FlowLayout.LEFT)
        linePanel.add(useCustomColorCheckBox)
        linePanel.add(colorPanel)

        panel.add(linePanel)

        useCustomColorCheckBox.addActionListener { _ ->
            colorPanel.isEnabled = useCustomColorCheckBox.isSelected
            if (useCustomColorCheckBox.isSelected && colorPanel.selectedColor == null) {
                colorPanel.selectedColor = JBColor.RED
            }
        }

        panel
    }

    override fun getDisplayName(): String = "Line Numbers"

    override fun createComponent(): JComponent = mainPanel

    override fun isModified(): Boolean {
        val settings = LineNumberSettings.getInstance()
        val currentMode = when (modeComboBox.selectedIndex) {
            0 -> LineNumberSettings.LineNumberMode.RELATIVE
            1 -> LineNumberSettings.LineNumberMode.ABSOLUTE
            else -> LineNumberSettings.LineNumberMode.HYBRID
        }
        val currentColor = if (useCustomColorCheckBox.isSelected) {
            colorPanel.selectedColor?.let { ColorUtil.toHex(it) }
        } else {
            null
        }

        return enabledCheckBox.isSelected != settings.isEnabled ||
                hideNativeCheckBox.isSelected != settings.isHideNativeLineNumbers ||
                currentMode != settings.lineNumberMode ||
                currentColor != settings.currentLineColor
    }

    override fun apply() {
        val settings = LineNumberSettings.getInstance()
        settings.isEnabled = enabledCheckBox.isSelected
        settings.isHideNativeLineNumbers = hideNativeCheckBox.isSelected
        settings.lineNumberMode = when (modeComboBox.selectedIndex) {
            0 -> LineNumberSettings.LineNumberMode.RELATIVE
            1 -> LineNumberSettings.LineNumberMode.ABSOLUTE
            else -> LineNumberSettings.LineNumberMode.HYBRID
        }

        val currentColor = if (useCustomColorCheckBox.isSelected) {
            colorPanel.selectedColor?.let { ColorUtil.toHex(it) }
        } else {
            null
        }
        settings.currentLineColor = currentColor

        val manager = ApplicationManager.getApplication().getService(LineNumberManager::class.java)
        manager.onSettingsChanged()
    }

    override fun reset() {
        val settings = LineNumberSettings.getInstance()
        enabledCheckBox.isSelected = settings.isEnabled
        hideNativeCheckBox.isSelected = settings.isHideNativeLineNumbers
        modeComboBox.selectedIndex = when (settings.lineNumberMode) {
            LineNumberSettings.LineNumberMode.RELATIVE -> 0
            LineNumberSettings.LineNumberMode.ABSOLUTE -> 1
            LineNumberSettings.LineNumberMode.HYBRID -> 2
        }

        // Reset color
        val colorHex = settings.currentLineColor
        if (colorHex != null) {
            useCustomColorCheckBox.isSelected = true
            colorPanel.selectedColor = ColorUtil.fromHex(colorHex)
            colorPanel.isEnabled = true
        } else {
            useCustomColorCheckBox.isSelected = false
            colorPanel.selectedColor = null
            colorPanel.isEnabled = false
        }
    }
}
