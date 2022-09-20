package space.active.taskmanager1c.domain.repository

import space.active.taskmanager1c.domain.utils.Resource

interface Authorization {
    suspend fun onServer(username: String, password: String): Resource<Boolean>
}