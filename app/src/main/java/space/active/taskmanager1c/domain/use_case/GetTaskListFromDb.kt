package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.repository.TasksRepository
import javax.inject.Inject


class GetTaskListFromDb @Inject constructor(
    private val repository: TasksRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    operator fun invoke() = flow<Request<String>> {
        // Получаем список задач.
        // Обрабатываем исключения. If Empty, another
        // Отправляем в готовом виде для отображения

        emit(PendingRequest())

        repository.listTasksFlow
            .catch { e ->
                emit(ErrorRequest(e))
            }
            .map {
                if (it.isNotEmpty()) {
                    SuccessRequest(it)
                } else {
                    ErrorRequest(EmptyObject)
                }
            }
            .collect { request ->
                when (request) {
                    is SuccessRequest -> {
                        emit(SuccessRequest(request.data.map { it.name }.toString()))
                    }
                    is PendingRequest -> {
                        emit(PendingRequest())
                    }
                    is ErrorRequest -> {
                        emit(ErrorRequest(request.exception))
                    }
                }
            }
    }.flowOn(ioDispatcher)
}