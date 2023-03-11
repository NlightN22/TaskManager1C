package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.TaskDomain
import space.active.taskmanager1c.domain.repository.TasksRepository
import javax.inject.Inject

private const val TAG = "SaveTaskChangesToDb"

class SaveTaskChangesToDb @Inject constructor(
    private val repository: TasksRepository,
    private val exceptionHandler: ExceptionHandler,
    private val logger: Logger
) {
    suspend operator fun invoke(taskDomain: TaskDomain, myId: String) {
        logger.log(TAG, "SaveTaskChangesToDb: $taskDomain")
        repository.editTask(taskDomain, myId).collectLatest {
            if (it is ErrorRequest) {
                exceptionHandler(it.exception)
            }
        }
    }
}