package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.domain.models.Credentials
import space.active.taskmanager1c.domain.models.UserDomain

interface SettingsRepository {
    suspend fun getUser(): UserDomain
    fun getUserFlow(): Flow<UserDomain>
    fun saveUser(userDomain: UserDomain): Flow<Request<Any>>
    fun savePassword(pass: String): Flow<Request<Any>>
    suspend fun getServerAddress(): String
    fun saveServerAddress(serverAddress: String): Flow<Request<Any>>
    fun clearSettings(): Flow<Request<Any>>
    fun getCredentials(): Flow<Credentials>
}