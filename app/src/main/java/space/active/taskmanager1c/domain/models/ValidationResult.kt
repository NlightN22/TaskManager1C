package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.coreutils.UiText

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(
        val message: UiText
    ): ValidationResult()
}
