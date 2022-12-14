package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TasksReadingTimeDTO
import space.active.taskmanager1c.domain.models.Credentials
import java.time.LocalDateTime

interface MessagesRepository {
    fun getMessagesReadingStatus( credentials: Credentials ,taskListIds: List<String>): Flow<Request<List<TasksReadingTimeDTO>>>
    fun getTaskMessages( credentials: Credentials, taskId: String): Flow<Request<TaskMessagesDTO>>
    fun sendNewMessage( credentials: Credentials, taskId: String, text: String): Flow<Request<TaskMessagesDTO>>
    fun sendReadingStatus( credentials: Credentials,taskId: String, dataTime: LocalDateTime): Flow<Request<Any>>
    fun sendNotReadingFlag( credentials: Credentials,taskId: String, state: Boolean): Flow<Request<Any>>
}