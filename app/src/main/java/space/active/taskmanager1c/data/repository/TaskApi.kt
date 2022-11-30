package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.data.remote.dto.TaskListDto

interface TaskApi {
    fun getTaskListFlow(): Flow<Request<TaskListDto>>
    suspend fun getTaskList(): Request<TaskListDto>
    suspend fun sendTaskChanges(task: TaskDto): Request<TaskDto>
    suspend fun authUser(username: String, password: String)
}