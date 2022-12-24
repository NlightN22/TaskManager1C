package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.data.remote.dto.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.domain.repository.MessagesRepository
import javax.inject.Inject

class MessagesRepositoryImpl @Inject constructor(
    private val taskApi: TaskApi): MessagesRepository {
    override fun getMessagesReadingStatus(taskListIds: List<String>): Flow<Request<List<TaskMessagesDTO>>> {
        TODO("Not yet implemented")
    }

    override fun getTaskMessages(taskId: String): Flow<Request<TaskMessagesDTO>> = flow {
        emit(PendingRequest())
        emit(SuccessRequest(taskApi.getMessages(taskId)))
    }

    override fun sendNewMessage(taskId: String): Flow<Request<Any?>> {
        TODO("Not yet implemented")
    }

    override fun sendReadingStatus(taskId: String): Flow<Request<Any?>> {
        TODO("Not yet implemented")
    }

    override fun sendNotReadingFlag(taskId: String): Flow<Request<Any?>> {
        TODO("Not yet implemented")
    }
}