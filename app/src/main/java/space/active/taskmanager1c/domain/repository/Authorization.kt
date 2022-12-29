package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.model.AuthUser


interface Authorization {
    fun auth(username: String, password: String): Flow<Request<AuthUser>>
}