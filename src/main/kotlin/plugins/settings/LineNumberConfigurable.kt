package plugins.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.VerticalFlowLayout
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import plugins.editor.LineNumberManager
import javax.swing.JComponent
import javax.swing.JPanel

class LineNumberConfigurable : Configurable {
    private val enabledCheckBox = JBCheckBox("Enable Relative Line Numbers")
    private val hideNativeCheckBox = JBCheckBox("Hide IDE's built-in line numbers")

    private val mainPanel: JPanel by lazy {
        val panel = JPanel(VerticalFlowLayout(VerticalFlowLayout.TOP))
        panel.border = JBUI.Borders.empty(5, 10)

        panel.add(JBLabel("Line Numbers Settings"))
        panel.add(enabledCheckBox)
        panel.add(hideNativeCheckBox)

        panel
    }

    override fun getDisplayName(): String = "Line Numbers"

    override fun createComponent(): JComponent = mainPanel

    override fun isModified(): Boolean {
        val settings = LineNumberSettings.getInstance()
        return enabledCheckBox.isSelected != settings.isEnabled ||
                hideNativeCheckBox.isSelected != settings.isHideNativeLineNumbers
    }

    override fun apply() {
        val settings = LineNumberSettings.getInstance()
        settings.isEnabled = enabledCheckBox.isSelected
        settings.isHideNativeLineNumbers = hideNativeCheckBox.isSelected

        val manager = ApplicationManager.getApplication().getService(LineNumberManager::class.java)
        manager.onSettingsChanged()
    }

    override fun reset() {
        val settings = LineNumberSettings.getInstance()
        enabledCheckBox.isSelected = settings.isEnabled
        hideNativeCheckBox.isSelected = settings.isHideNativeLineNumbers
    }
}