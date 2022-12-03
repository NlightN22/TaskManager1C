package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.repository.TasksRepository
import javax.inject.Inject

private const val TRIES_TO_FETCH = 10
private const val FETCH_TIMEOUT = 5000L

class HandleEmptyTaskList @Inject constructor(
    private val repository: TasksRepository,
    private val updateJob: HandleJobForUpdateDb,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke() = flow<Request<List<Task>>> {
        // Если список пустой, то мы пытаемся получить его с сервера
        //Если ответ с сервера успешный, то выводим информацию об отсутствии задач
        //Если нет, то обрабатываем исключения
        emit(PendingRequest())
        var listTasks: List<Task> = repository.listTasksFlow.first()
        if (listTasks.isEmpty()) {
            var iterator = 0
            while (iterator < TRIES_TO_FETCH) {
                updateJob.inputFetchJobFlow().collect { fetchResult ->
                    when (fetchResult) {
                        is SuccessRequest -> {
                            listTasks = repository.listTasksFlow.first()
                            if (listTasks.isEmpty()) {
                                emit(ErrorRequest(EmptyObject))
                                iterator = TRIES_TO_FETCH
                            } else if (listTasks.isNotEmpty()) {
                                iterator = TRIES_TO_FETCH
                            }
                        }
                        is PendingRequest -> {
                            emit(PendingRequest())
                        }
                        is ErrorRequest -> {
                            emit(ErrorRequest(fetchResult.exception))
                            iterator++
                            delay(FETCH_TIMEOUT)

//                            when (fetchResult.exception) {
//                                is NullAnswerFromServer -> {
//                                    emit(ErrorRequest(EmptyObject))
//                                }
//                                is HttpException -> {}
//                                else -> {} // TODO add classic exceptions
//                            }
                        }
                    }
                }
            }
        }
    }.flowOn(ioDispatcher)
}