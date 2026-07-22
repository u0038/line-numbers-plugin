package plugins.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import kotlin.jvm.java

@Service(Service.Level.APP)
@State(name = "LineNumberSettings", storages = [Storage("LineNumberSettings.xml")])
class LineNumberSettings : PersistentStateComponent<LineNumberSettings.State> {
    data class State(
        var enabled: Boolean = true,
        var showRelativeNumber: Boolean = true,
        var hideNativeLineNumbers: Boolean = false,
        var currentLineColor: String? = null
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
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

    var isShowRelativeNumber: Boolean
        get() = state.showRelativeNumber
        set(value) {
            state.showRelativeNumber = value
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