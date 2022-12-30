package space.active.taskmanager1c.domain.models

import kotlinx.serialization.Serializable
import space.active.taskmanager1c.coreutils.EmptyObject
import space.active.taskmanager1c.data.remote.model.AuthBasicDto

@Serializable
data class UserSettings(
    val username: String? = null,
    val userId: String? = null,
    val password: String? = null,
    val serverAddress: String? = null
    // TODO add base URL to settings and transfer it to class in parameters
) {
    fun toAuthBasicDto() = AuthBasicDto(
        name = this.username ?: throw EmptyObject("username"),
        pass = this.password ?: throw EmptyObject("password")
    )
}