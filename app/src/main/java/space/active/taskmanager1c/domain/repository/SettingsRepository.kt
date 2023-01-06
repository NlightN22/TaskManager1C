package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.domain.models.Credentials
import space.active.taskmanager1c.domain.models.User

interface SettingsRepository {
    suspend fun getUser(): User
    fun getUserFlow(): Flow<User>
    fun saveUser(user: User): Flow<Request<Any>>
    fun savePassword(pass: String): Flow<Request<Any>>
    suspend fun getServerAddress(): String?
    fun saveServerAddress(serverAddress: String): Flow<Request<Any>>
    fun clearSettings(): Flow<Request<Any>>
    fun getCredentials(): Flow<Credentials>
}