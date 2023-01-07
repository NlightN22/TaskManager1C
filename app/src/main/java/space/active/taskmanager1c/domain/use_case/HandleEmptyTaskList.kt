package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.Credentials
import space.active.taskmanager1c.domain.repository.UpdateJobInterface
import javax.inject.Inject

private const val TRIES_TO_FETCH = 10
private const val FETCH_TIMEOUT = 5000L
private const val TAG = "HandleEmptyTaskList"



class HandleEmptyTaskList @Inject constructor(
    private val updateJob: UpdateJobInterface,
    private val exceptionHandler: ExceptionHandler,
    private val logger: Logger,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    var tries = 0
    operator fun invoke(credentials: Credentials, whoAmI: UserInput) = flow<Request<Any>> {
        logger.log(TAG, "HandleEmptyTaskList launch")
        emit(PendingRequest())
        var successResult: Boolean = false
        while (tries <= TRIES_TO_FETCH && !successResult) {
            logger.log(TAG, "HandleEmptyTaskList try $tries ")
            try {
                updateJob.inputFetchJobFlow(credentials, whoAmI).collect { request ->
                    emit(PendingRequest())
                    when (request) {
                        is SuccessRequest -> {
                            logger.log(TAG, "HandleEmptyTaskList success ")
                            emit(request)
                            successResult = true
                        }
                        is PendingRequest -> {
                            emit(request)
                            delay(FETCH_TIMEOUT)
                        }
                        is ErrorRequest -> {
                            throw request.exception
                        }
                    }
                }
            } catch (e: Throwable) {
                exceptionHandler(e)
                delay(FETCH_TIMEOUT)
            }
            tries += 1
        }
        emit(SuccessRequest(Any()))
    }.flowOn(ioDispatcher)
}