package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.dto.messages_dto.TaskMessagesDTO

interface MessagesRepository {
    fun getMessagesReadingStatus(taskListIds: List<String>): Flow<Request<List<TaskMessagesDTO>>>
    fun getTaskMessages(taskId: String): Flow<Request<TaskMessagesDTO>>
    fun sendNewMessage(taskId: String): Flow<Request<Any?>>
    fun sendReadingStatus(taskId: String): Flow<Request<Any?>>
    fun sendNotReadingFlag(taskId: String): Flow<Request<Any?>>
}