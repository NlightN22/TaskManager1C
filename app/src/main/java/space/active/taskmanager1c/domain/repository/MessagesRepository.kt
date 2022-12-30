package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TasksReadingTimeDTO
import space.active.taskmanager1c.domain.models.UserSettings
import java.time.LocalDateTime

interface MessagesRepository {
    fun getMessagesReadingStatus( userSettings: UserSettings ,taskListIds: List<String>): Flow<Request<List<TasksReadingTimeDTO>>>
    fun getTaskMessages( userSettings: UserSettings, taskId: String): Flow<Request<TaskMessagesDTO>>
    fun sendNewMessage( userSettings: UserSettings, taskId: String, text: String): Flow<Request<TaskMessagesDTO>>
    fun sendReadingStatus( userSettings: UserSettings,taskId: String, dataTime: LocalDateTime): Flow<Request<Any>>
    fun sendNotReadingFlag( userSettings: UserSettings,taskId: String, state: Boolean): Flow<Request<Any>>
}