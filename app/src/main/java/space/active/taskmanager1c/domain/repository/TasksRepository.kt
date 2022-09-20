package space.active.taskmanager1c.domain.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.User

interface TasksRepository {
    val listTasks: Flow<List<Task>>
    fun getUser(userId: String): Flow<User>
    fun getTask(taskId: String): Flow<Task>
}