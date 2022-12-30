package space.active.taskmanager1c.data.remote

import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.model.AuthBasicDto
import space.active.taskmanager1c.data.remote.model.TaskDto
import space.active.taskmanager1c.data.remote.model.TaskListDto
import space.active.taskmanager1c.data.remote.model.UserDto
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskUserReadingFlagDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TasksReadingTimeDTO
import java.time.LocalDateTime

interface TaskApi {
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
    suspend fun getMessagesTimes(auth: AuthBasicDto, taskIds: List<String>): List<TasksReadingTimeDTO>
    suspend fun setReadingTime(auth: AuthBasicDto, taskId: String, readingTime: LocalDateTime): TasksReadingTimeDTO
    suspend fun setReadingFlag(auth: AuthBasicDto, taskId: String, flag: Boolean): TaskUserReadingFlagDTO
}