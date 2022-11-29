package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.data.remote.dto.TaskListDto

interface TaskApi {
    suspend fun getTaskList(): Flow<Request<TaskListDto>>
    suspend fun sendTaskChanges(task: TaskDto)
    suspend fun authUser(username: String, password: String)
}