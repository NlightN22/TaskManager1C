package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.coreutils.EmptyObject
import space.active.taskmanager1c.coreutils.EncryptedData
import space.active.taskmanager1c.data.remote.model.AuthBasicDto

data class Credentials(
    val username: EncryptedData,
    val password: EncryptedData
)
{
    fun toAuthBasicDto() = AuthBasicDto(
        name = this.username.getString() ?: throw EmptyObject("username"),
        pass = this.password.getString() ?: throw EmptyObject("password")
    )
}
