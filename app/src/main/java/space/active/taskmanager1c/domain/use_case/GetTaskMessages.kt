package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.repository.MessagesRepository
import javax.inject.Inject

class GetTaskMessages @Inject constructor(
    private val messagesRepository: MessagesRepository,
    private val exceptionHandler: ExceptionHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke(taskId: String): Flow<Request<TaskMessagesDTO>> =
        messagesRepository.getTaskMessages(taskId).catch { e ->
            exceptionHandler(e)
        }.flowOn(ioDispatcher)
}