package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.domain.models.UserSettings

interface UpdateJobInterface {
    fun updateJob(userSettings: UserSettings, updateDelay: Long): Flow<Request<Any>>
    fun inputFetchJobFlow(userSettings: UserSettings,): Flow<Request<Any>>
}