package plugins.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(name = "LineNumberSettings", storages = [Storage("LineNumberSettings.xml")])
class LineNumberSettings : PersistentStateComponent<LineNumberSettings.State> {
    
    enum class LineNumberMode {
        RELATIVE,
        ABSOLUTE,
        HYBRID
    }

    data class State(
        var enabled: Boolean = true,
        var showRelativeNumber: Boolean = true, // Maintained for backwards-compatibility migration
        var lineNumberMode: LineNumberMode = LineNumberMode.RELATIVE,
        var hideNativeLineNumbers: Boolean = false,
        var currentLineColor: String? = null
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
        // Migrate old settings: if showRelativeNumber was false, we switch to ABSOLUTE
        if (!state.showRelativeNumber && state.lineNumberMode == LineNumberMode.RELATIVE) {
            state.lineNumberMode = LineNumberMode.ABSOLUTE
        }
    }

    companion object {
        fun getInstance(): LineNumberSettings {
            return ApplicationManager.getApplication().getService(LineNumberSettings::class.java)
        }
    }

    var isEnabled: Boolean
        get() = state.enabled
        set(value) {
            state.enabled = value
        }

    var lineNumberMode: LineNumberMode
        get() = state.lineNumberMode
        set(value) {
            state.lineNumberMode = value
            // Sync legacy field for backwards compatibility
            state.showRelativeNumber = (value != LineNumberMode.ABSOLUTE)
        }

    var isHideNativeLineNumbers: Boolean
        get() = state.hideNativeLineNumbers
        set(value) {
            state.hideNativeLineNumbers = value
        }

    var currentLineColor: String?
        get() = state.currentLineColor
        set(value) {
            state.currentLineColor = value
        }
}
