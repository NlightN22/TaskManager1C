package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.data.remote.model.reading_times.ReadingTimesTask
import space.active.taskmanager1c.domain.models.Credentials
import java.time.LocalDateTime

interface MessagesRepository {
    fun getTaskMessages(credentials: Credentials, taskId: String): Flow<Request<TaskMessagesDTO>>
    fun sendNewMessage(
        credentials: Credentials,
        taskId: String,
        text: String
    ): Flow<Request<TaskMessagesDTO>>

    fun sendReadingTime(
        credentials: Credentials,
        taskId: String,
        messageReadingTime: LocalDateTime,
        taskReadingTime: LocalDateTime
    ): Flow<Request<ReadingTimesTask>>

    fun sendNotReadingFlag(
        credentials: Credentials,
        taskId: String,
        state: Boolean
    ): Flow<Request<Any>>
}