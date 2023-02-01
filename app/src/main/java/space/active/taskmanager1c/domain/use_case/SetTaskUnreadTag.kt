package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskUserReadingFlagDTO
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.Credentials
import space.active.taskmanager1c.domain.repository.MessagesRepository
import javax.inject.Inject

private const val TAG = "SetTaskUnreadTag"

class SetTaskUnreadTag @Inject constructor(
    private val logger: Logger,
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
            inputTaskRepository.getTask(it.data.id)?.taskInput?.let { task ->
                logger.log(TAG, "new version : ${it.data.version}")
                logger.log(TAG, "cur version : ${task.version}")
                if (it.data.version > task.version) {
                    inputTaskRepository.setUnreadTag(it.data.id, it.data.version, it.data.flagToBoolean())
                }
            }
        }
    }.flowOn(ioDispatcher)
}