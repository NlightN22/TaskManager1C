package space.active.taskmanager1c.presentation.screens.settings

import space.active.taskmanager1c.coreutils.UiText

data class SettingsViewState(
    val userId: String = "",
    val userName: String = "",
    val serverAddress: String = "",
    val editServerAddress: Boolean = false,
    val skipStatusAlert: Boolean = false,
    val addressError: UiText? = null,
)

data class SettingsVisibleState(
    val userId: Boolean = true,
    val userName: Boolean = true,
    val serverAddress: Boolean = true,
    val skipStatusAlert: Boolean = false
) {
    companion object {
        fun getIsLogin(boolean: Boolean): SettingsVisibleState = if (boolean) {
            SettingsVisibleState(skipStatusAlert = true)
        } else {
            SettingsVisibleState(skipStatusAlert = false)
        }
    }
}
