package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.model.TaskDto
import space.active.taskmanager1c.data.remote.model.TaskListDto
import space.active.taskmanager1c.data.remote.model.UserDto
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO

interface TaskApi {
    fun getTaskListFlow(): Flow<Request<TaskListDto>>
    suspend fun authUser(): UserDto
    suspend fun getTaskList(): Request<TaskListDto>
    suspend fun sendNewTask(task: TaskDto): Request<TaskDto>
    suspend fun sendEditedTaskMappedChanges(taskId: String, changeMap: Map<String, Any>): TaskDto
    suspend fun getMessages(taskId: String): TaskMessagesDTO
    suspend fun sendMessage(taskId: String, text: String): TaskMessagesDTO
    suspend fun getMessagesTimes(taskIds: List<String>): List<TaskMessagesDTO>
}