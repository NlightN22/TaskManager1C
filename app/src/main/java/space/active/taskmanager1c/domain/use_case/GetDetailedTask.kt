package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.repository.InputTaskRepository
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.repository.TasksRepository
import javax.inject.Inject

class GetDetailedTask @Inject constructor(
    private val repository: TasksRepository,
    private val inputTaskRepository: InputTaskRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
        operator fun invoke(taskId: String) = repository.getTask(taskId).flowOn(ioDispatcher)
//    operator fun invoke(taskId: String) = flow<Request<TaskInput>> {
//        val res = inputTaskRepository.getTask(taskId)
//        emit(SuccessRequest(res))
//    }.catch { e ->
//        ErrorRequest<TaskInput>(e)
//    }.flowOn(ioDispatcher)
}