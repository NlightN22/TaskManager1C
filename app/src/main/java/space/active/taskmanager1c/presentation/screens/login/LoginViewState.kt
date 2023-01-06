package space.active.taskmanager1c.presentation.screens.login

import space.active.taskmanager1c.coreutils.UiText

data class LoginViewState(
    val username: String = "",
    val password: String = "",
    val userError: UiText? = null,
    val passError: UiText? = null
)