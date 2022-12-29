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
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.UserSettings
import space.active.taskmanager1c.domain.repository.TasksRepository
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
    operator fun invoke(userSettings: UserSettings) = flow<Request<Any>> {
        // Если список пустой, то мы пытаемся получить его с сервера
        //Если ответ с сервера успешный, то выводим информацию об отсутствии задач
        //Если нет, то обрабатываем исключения
        logger.log(TAG, "HandleEmptyTaskList launch")
        emit(PendingRequest())
        var successResult: Boolean = false
        while (tries <= TRIES_TO_FETCH && !successResult) {
            logger.log(TAG, "HandleEmptyTaskList try $tries ")
            try {
                updateJob.inputFetchJobFlow(userSettings).collect { request ->
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

//        var listTasks: List<Task> = repository.listTasksFlow.first()
//        if (listTasks.isEmpty()) {
//            var iterator = 0
//            while (iterator < TRIES_TO_FETCH) {
//                updateJob.inputFetchJobFlow(userSettings).collect { fetchResult ->
//                    when (fetchResult) {
//                        is SuccessRequest -> {
//                            listTasks = repository.listTasksFlow.first()
//                            if (listTasks.isEmpty()) {
//                                emit(ErrorRequest(EmptyObject("listTasks")))
//                                iterator = TRIES_TO_FETCH
//                            } else if (listTasks.isNotEmpty()) {
//                                iterator = TRIES_TO_FETCH
//                            }
//                        }
//                        is PendingRequest -> {
//                            emit(PendingRequest())
//                        }
//                        is ErrorRequest -> {
//                            emit(ErrorRequest(fetchResult.exception))
//                            iterator++
//                            delay(FETCH_TIMEOUT)
//
////                            when (fetchResult.exception) {
////                                is NullAnswerFromServer -> {
////                                    emit(ErrorRequest(EmptyObject))
////                                }
////                                is HttpException -> {}
////                                else -> {} // TODO add classic exceptions
////                            }
//                        }
//                    }
//                }
//            }