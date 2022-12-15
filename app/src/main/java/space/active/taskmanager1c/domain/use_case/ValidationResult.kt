package space.active.taskmanager1c.domain.use_case

sealed class ValidationResult {
    object Success : ValidationResult ()
    data class Error(
        val message: String
    ): ValidationResult()
}
