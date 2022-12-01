package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request

interface UpdateJobHandler {
    fun updateJob(): Flow<Request<Any>>
    suspend fun inputFetchJob(): Request<Any>
}