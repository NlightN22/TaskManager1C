package space.active.taskmanager1c.presentation.screens.task_detailed

import space.active.taskmanager1c.coreutils.UiText

sealed class EditTextErrors {
    data class Title(val message: UiText): EditTextErrors()
}