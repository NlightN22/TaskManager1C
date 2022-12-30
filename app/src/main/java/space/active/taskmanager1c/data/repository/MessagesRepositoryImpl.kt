package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.data.remote.TaskApi
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TasksReadingTimeDTO
import space.active.taskmanager1c.domain.models.UserSettings
import space.active.taskmanager1c.domain.repository.MessagesRepository
import java.time.LocalDateTime
import javax.inject.Inject

class MessagesRepositoryImpl @Inject constructor(
    private val taskApi: TaskApi
) : MessagesRepository {
    override fun getMessagesReadingStatus(
        userSettings: UserSettings,
        taskListIds: List<String>
    ): Flow<Request<List<TasksReadingTimeDTO>>> {
        TODO("Not yet implemented")
    }

    override fun getTaskMessages(
        userSettings: UserSettings,
        taskId: String
    ): Flow<Request<TaskMessagesDTO>> = flow {
        emit(PendingRequest())
        emit(SuccessRequest(taskApi.getMessages(userSettings.toAuthBasicDto(), taskId)))
    }

    override fun sendNewMessage(
        userSettings: UserSettings,
        taskId: String,
        text: String
    ): Flow<Request<TaskMessagesDTO>> = flow {
        emit(PendingRequest())
        emit(SuccessRequest(taskApi.sendMessage(userSettings.toAuthBasicDto(), taskId, text)))
    }

    override fun sendReadingStatus(
        userSettings: UserSettings,
        taskId: String,
        dataTime: LocalDateTime
    ): Flow<Request<Any>> {
        TODO("Not yet implemented")
    }

    override fun sendNotReadingFlag(
        userSettings: UserSettings,
        taskId: String,
        state: Boolean
    ): Flow<Request<Any>> {
        TODO("Not yet implemented")
    }


}