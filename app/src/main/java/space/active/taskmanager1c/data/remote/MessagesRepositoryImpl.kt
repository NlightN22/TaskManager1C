package space.active.taskmanager1c.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.data.repository.TaskApi
import space.active.taskmanager1c.domain.repository.MessagesRepository
import java.time.LocalDateTime
import javax.inject.Inject

class MessagesRepositoryImpl @Inject constructor(
    private val taskApi: TaskApi
): MessagesRepository {
    override fun getMessagesReadingStatus(taskListIds: List<String>): Flow<Request<List<TaskMessagesDTO>>> {
        TODO("Not yet implemented")
    }

    override fun getTaskMessages(taskId: String): Flow<Request<TaskMessagesDTO>> = flow {
        emit(PendingRequest())
        emit(SuccessRequest(taskApi.getMessages(taskId)))
    }

    override fun sendNewMessage(taskId: String, text: String): Flow<Request<TaskMessagesDTO>> = flow {
        emit(PendingRequest())
        emit(SuccessRequest(taskApi.sendMessage(taskId, text)))
    }

    override fun sendReadingStatus(taskId: String, dataTime: LocalDateTime): Flow<Request<Any?>> {
        TODO("Not yet implemented")
    }

    override fun sendNotReadingFlag(taskId: String, state: Boolean): Flow<Request<Any?>> {
        TODO("Not yet implemented")
    }
}