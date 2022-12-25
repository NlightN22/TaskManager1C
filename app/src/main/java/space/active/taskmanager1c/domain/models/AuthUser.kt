package space.active.taskmanager1c.domain.models

data class AuthUser(
    val tokenId: String,
    val userId: String,
    val userName: String,
    val pass: String
)
