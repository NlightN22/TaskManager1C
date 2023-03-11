package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.repository.MessagesRepository
import javax.inject.Inject

private const val TAG = "GetTaskUnreadStatus"

class GetTaskUnreadStatus @Inject constructor(
    private val messagesRepository: MessagesRepository,
    private val logger: Logger,
    private val credentials: GetCredentials,
    private val exceptionHandler: ExceptionHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val updateDelay: Long = 1000
    suspend operator fun invoke(taskId: String): Flow<Boolean> = flow {
        while (true) {
            messagesRepository.getUnreadTaskIds(credentials(), listOf(taskId))
                .catch { exceptionHandler(it) }
                .collect { request ->
                    when (request) {
                        is SuccessRequest -> {
                            emit(request.data.isNotEmpty())
                        }
                        is ErrorRequest -> {}
                        is PendingRequest -> {}
                    }
                }
            delay(updateDelay)
        }
    }.flowOn(ioDispatcher)
}