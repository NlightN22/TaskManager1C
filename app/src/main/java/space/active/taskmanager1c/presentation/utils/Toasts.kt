package space.active.taskmanager1c.presentation.utils

import space.active.taskmanager1c.coreutils.UiText

interface Toasts {
    operator fun invoke(message: String)
    operator fun invoke(uiText: UiText)
}