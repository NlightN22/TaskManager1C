package space.active.taskmanager1c.presentation.screens.settings

import space.active.taskmanager1c.coreutils.UiText

data class SettingsViewState(
    val userId: String = "",
    val userName: String = "",
    val serverAddress: String ="",
    val editServerAddress: Boolean = false,
    val addressError: UiText? = null
)
