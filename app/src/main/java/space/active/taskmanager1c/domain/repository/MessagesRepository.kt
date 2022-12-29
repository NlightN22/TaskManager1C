package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO
import java.time.LocalDateTime

interface MessagesRepository {
    fun getMessagesReadingStatus(taskListIds: List<String>): Flow<Request<List<TaskMessagesDTO>>>
    fun getTaskMessages(taskId: String): Flow<Request<TaskMessagesDTO>>
    fun sendNewMessage(taskId: String, text: String): Flow<Request<TaskMessagesDTO>>
    fun sendReadingStatus(taskId: String, dataTime: LocalDateTime): Flow<Request<Any?>>
    fun sendNotReadingFlag(taskId: String, state: Boolean): Flow<Request<Any?>>
}