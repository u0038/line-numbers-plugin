package plugins.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.ColorPanel
import com.intellij.ui.ColorUtil
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import plugins.editor.LineNumberManager
import java.awt.Color
import javax.swing.*

class LineNumberConfigurable : Configurable {
    private val enabledCheckBox = JBCheckBox("Enable Relative Line Numbers")
    private val hideNativeCheckBox = JBCheckBox("Hide IDE's built-in line numbers")
    
    // Current line color components using standard ColorPanel
    private val useCustomColorCheckBox = JBCheckBox("Foreground")
    private val colorPanel = ColorPanel()

    private val mainPanel: JPanel by lazy {
        val panel = JPanel(VerticalFlowLayout(VerticalFlowLayout.TOP))
        panel.border = JBUI.Borders.empty(10)

        panel.add(JBLabel("Line Numbers Settings"))
        panel.add(enabledCheckBox)
        panel.add(hideNativeCheckBox)
        
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
                colorPanel.selectedColor = Color.RED
            }
        }

        panel
    }

    override fun getDisplayName(): String = "Line Numbers"

    override fun createComponent(): JComponent = mainPanel

    override fun isModified(): Boolean {
        val settings = LineNumberSettings.getInstance()
        val currentColor = if (useCustomColorCheckBox.isSelected) {
            colorPanel.selectedColor?.let { ColorUtil.toHex(it) }
        } else {
            null
        }
        
        return enabledCheckBox.isSelected != settings.isEnabled ||
                hideNativeCheckBox.isSelected != settings.isHideNativeLineNumbers ||
                currentColor != settings.currentLineColor
    }

    override fun apply() {
        val settings = LineNumberSettings.getInstance()
        settings.isEnabled = enabledCheckBox.isSelected
        settings.isHideNativeLineNumbers = hideNativeCheckBox.isSelected
        
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
