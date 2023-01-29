package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.domain.models.Credentials

interface UpdateJobInterface {
    fun updateJob(credentials: Credentials, updateDelay: Long, whoAmI: UserInput,
                  skippedExceptions: Flow<List<Throwable>>): Flow<Request<Any>>
    fun inputFetchJobFlow(credentials: Credentials, whoAmI: UserInput): Flow<Request<List<String>>>
}