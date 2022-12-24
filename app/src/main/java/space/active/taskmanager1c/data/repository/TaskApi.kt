package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.dto.AuthDto
import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.data.remote.dto.TaskListDto
import space.active.taskmanager1c.data.remote.dto.messages_dto.TaskMessagesDTO

interface TaskApi {
    fun getTaskListFlow(): Flow<Request<TaskListDto>>
    suspend fun getTaskList(): Request<TaskListDto>
    suspend fun sendNewTask(task: TaskDto): Request<TaskDto>
    suspend fun sendEditedTaskMappedChanges(taskId: String, changeMap: Map<String, Any?>): TaskDto
    suspend fun authUser(username: String, password: String): Request<AuthDto>
    suspend fun getMessages(taskId: String): TaskMessagesDTO
    suspend fun getMessagesTimes(taskIds: List<String>): List<TaskMessagesDTO>
}