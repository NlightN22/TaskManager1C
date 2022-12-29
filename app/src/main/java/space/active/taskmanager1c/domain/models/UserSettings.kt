package space.active.taskmanager1c.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val username: String? = null,
    val userId: String? = null,
    val password: String? = null
)