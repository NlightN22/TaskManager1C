package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.lastOrNull
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.ServerNoAnswer
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.repository.TasksRepository
import space.active.taskmanager1c.domain.repository.UpdateJobHandler

private const val TRIES_TO_FETCH = 10
private const val FETCH_TIMEOUT = 5000L

class GetFirstListUseCase(
    private val repository: TasksRepository,
    private val updateJobHandler: UpdateJobHandler
) {

    suspend operator fun invoke(): Flow<Request<List<Task>>> {
        val iterator = 0
        var listTasks: List<Task> = repository.listTasksFlow.last()
        while (iterator <= TRIES_TO_FETCH || listTasks.isNotEmpty()) {
            updateJobHandler.inputFetchJob()
            listTasks = repository.listTasksFlow.last()
            delay(FETCH_TIMEOUT)
        }
        if (listTasks.isNotEmpty()) {
            return flow { SuccessRequest(listTasks) }
        }
        return flow { ErrorRequest<List<Task>>(ServerNoAnswer) }
    }
}