package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.remote.dto.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.User

interface TasksRepository {
    val listTasksFlow: Flow<List<Task>>
    val listUsersFlow: Flow<List<User>>
    fun getTask(taskId: String): Flow<Task?>
    fun editTask(task: Task): Flow<Request<Any>>
    fun createNewTask(task: Task): Flow<Request<Any>>
    fun attachFileToTask(file: ByteArray, taskId: String): Flow<Request<Any>>
}