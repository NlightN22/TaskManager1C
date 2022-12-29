package space.active.taskmanager1c.data.remote

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.model.AuthBasicDto
import space.active.taskmanager1c.data.remote.model.TaskDto
import space.active.taskmanager1c.data.remote.model.TaskListDto
import space.active.taskmanager1c.data.remote.model.UserDto
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO

interface TaskApi {
    fun getTaskListFlow(auth: AuthBasicDto): Flow<Request<TaskListDto>>
    suspend fun authUser(auth: AuthBasicDto): UserDto
    suspend fun getTaskList(auth: AuthBasicDto): Request<TaskListDto>
    suspend fun sendNewTask(auth: AuthBasicDto, task: TaskDto): Request<TaskDto>
    suspend fun sendEditedTaskMappedChanges(
        auth: AuthBasicDto,
        taskId: String,
        changeMap: Map<String, Any>
    ): TaskDto

    suspend fun getMessages(auth: AuthBasicDto, taskId: String): TaskMessagesDTO
    suspend fun sendMessage(auth: AuthBasicDto, taskId: String, text: String): TaskMessagesDTO
    suspend fun getMessagesTimes(auth: AuthBasicDto, taskIds: List<String>): List<TaskMessagesDTO>
}