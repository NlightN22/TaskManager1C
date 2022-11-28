package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.domain.models.Task

interface TasksRepository {
    val listTasks: Flow<Request<List<Task>>>
    fun getTask(taskId: String): Flow<Request<Task>>
    fun editTask(task: Task): Flow<Request<Any>>
    fun createNewTask(task: Task): Flow<Request<Any>>
    fun attachFileToTask(file: ByteArray, taskId: String): Flow<Request<Any>>
}