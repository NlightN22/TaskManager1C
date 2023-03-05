package space.active.taskmanager1c.data.remote.model

import okhttp3.Credentials
import java.nio.charset.StandardCharsets

data class AuthBasicDto (
    val name: String,
    val pass: String
        )
{
    fun toBasic(): String =
        Credentials.basic(this.name, this.pass, StandardCharsets.UTF_8)
}