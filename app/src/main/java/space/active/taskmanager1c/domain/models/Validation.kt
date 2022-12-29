package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.coreutils.UiText


data class Validation (
    val success: Boolean,
    val errorMessage: UiText? = null
)
