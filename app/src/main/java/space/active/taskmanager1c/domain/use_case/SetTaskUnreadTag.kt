package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskUserReadingFlagDTO
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.Credentials
import space.active.taskmanager1c.domain.repository.MessagesRepository
import javax.inject.Inject

class SetTaskUnreadTag @Inject constructor(
    private val messagesRepository: MessagesRepository,
    private val inputTaskRepository: InputTaskRepository,
    private val exceptionHandler: ExceptionHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke(
        credentials: Credentials,
        taskId: String,
        state: Boolean
    ): Flow<Request<TaskUserReadingFlagDTO>> = messagesRepository.sendNotReadingFlag(
        credentials,
        taskId,
        state
    ).catch {
        exceptionHandler(it)
    }.onEach {
        if (it is SuccessRequest) {
            inputTaskRepository.setUnreadTag(it.data.id, it.data.flagToBoolean())
        }
    }.flowOn(ioDispatcher)
}