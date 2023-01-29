package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskUserReadingFlagDTO
import space.active.taskmanager1c.data.remote.model.reading_times.ReadingTimesTaskDTO
import space.active.taskmanager1c.domain.models.Credentials
import java.time.LocalDateTime

interface MessagesRepository {
    fun getTaskMessages(credentials: Credentials, taskId: String): Flow<Request<TaskMessagesDTO>>

    fun getUnreadTaskIds(credentials: Credentials, fetchTaskIds: List<String>): Flow<Request<List<String>>>

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
    ): Flow<Request<ReadingTimesTaskDTO>>

    fun sendNotReadingFlag(
        credentials: Credentials,
        taskId: String,
        state: Boolean
    ): Flow<Request<TaskUserReadingFlagDTO>>
}