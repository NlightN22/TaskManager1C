package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.repository.TasksRepository
import javax.inject.Inject

class GetDetailedTask @Inject constructor(
    private val repository: TasksRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
        operator fun invoke(taskId: String): Flow<Task?> = repository.getTask(taskId).flowOn(ioDispatcher)
//    operator fun invoke(taskId: String) = flow<Request<TaskInput>> {
//        val res = inputTaskRepository.getTask(taskId)
//        emit(SuccessRequest(res))
//    }.catch { e ->
//        ErrorRequest<TaskInput>(e)
//    }.flowOn(ioDispatcher)
}
