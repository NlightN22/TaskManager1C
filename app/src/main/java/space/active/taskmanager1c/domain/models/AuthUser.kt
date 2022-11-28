package space.active.taskmanager1c.domain.models

import com.google.gson.reflect.TypeToken

data class AuthUser(
    val result: Boolean,
    val token_id: String,
    val user_id: String,
)
