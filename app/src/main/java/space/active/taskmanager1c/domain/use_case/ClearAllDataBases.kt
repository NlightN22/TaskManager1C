package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.local.OutputTaskRepository
import space.active.taskmanager1c.data.local.db.tasks_room_db.TaskWithUsersDatabase
import space.active.taskmanager1c.di.IoDispatcher
import javax.inject.Inject

class ClearAllTables @Inject constructor(
    private val taskWithUsersDatabase: TaskWithUsersDatabase,
    private val inputTasks: InputTaskRepository,
    private val outputTask: OutputTaskRepository,
    private val exceptionHandler: ExceptionHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke() = flow<Boolean> {
        emit(false)
        do {
            taskWithUsersDatabase.clearAllTables()
            delay(500)
        } while (!checkLists())
        emit(true)
    }
        .catch { exceptionHandler(it) }
        .flowOn(ioDispatcher)

    private suspend fun checkLists(): Boolean {
        val inputList = inputTasks.getTasks()
        val outputList = outputTask.getTasks()
        return inputList.isEmpty() && outputList.isEmpty()
    }
}