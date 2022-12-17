package space.active.taskmanager1c.domain.models

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(
        val message: String
    ): ValidationResult()
}
