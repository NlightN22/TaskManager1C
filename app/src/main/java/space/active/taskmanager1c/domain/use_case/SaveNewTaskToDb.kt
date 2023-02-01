package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.TaskDomain
import space.active.taskmanager1c.domain.repository.TasksRepository
import javax.inject.Inject

class SaveNewTaskToDb @Inject constructor(
    private val repository: TasksRepository,
    private val exceptionHandler: ExceptionHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke(taskDomain: TaskDomain, myId:String): Flow<Request<Any>> =
        repository.createNewTask(taskDomain, myId)
            .catch {
                exceptionHandler(it)
            }
            .flowOn(ioDispatcher)
}