package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.TasksFromRemote.TasksFromRemoteDb
import space.active.taskmanager1c.data.remote.TaskApi
import space.active.taskmanager1c.domain.models.Messages
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.User
import space.active.taskmanager1c.domain.repository.TasksRepository

class TaskRepositoryImpl(
    val taskDb: TasksFromRemoteDb,
    val api: TaskApi
): TasksRepository {
    override val listTasks: Flow<List<Task>>
        get() = TODO("Not yet implemented")

    override fun getUser(userId: String): Flow<User> {
        TODO("Not yet implemented")
    }

    override fun getTask(taskId: String): Flow<Task> {
        TODO("Not yet implemented")
    }

    override fun getMessages(taskId: String): Flow<Messages> {
        TODO("Not yet implemented")
    }
}