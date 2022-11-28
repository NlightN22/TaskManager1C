package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.domain.models.AuthUser


interface Authorization {
    fun onServer(username: String, password: String): Flow<Request<AuthUser>>
}