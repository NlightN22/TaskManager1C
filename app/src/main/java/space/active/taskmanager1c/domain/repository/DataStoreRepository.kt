package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow

interface DataStoreRepository {
    suspend fun setUserName(userName: String)
    fun getUserName(): Flow<String?>
    suspend fun setUserId(userId: String)
    fun getUserId(): Flow<String?>
    suspend fun setUserPass(userPass: String)
    fun getUserPass(): Flow<String?>
}