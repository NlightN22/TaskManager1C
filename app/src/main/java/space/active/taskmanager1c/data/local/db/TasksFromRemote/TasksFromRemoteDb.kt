package space.active.taskmanager1c.data.local.db.TasksFromRemote

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.TasksFromRemote.entity.MessageDb
import space.active.taskmanager1c.data.local.db.TasksFromRemote.entity.TaskDb
import space.active.taskmanager1c.data.local.db.TasksFromRemote.entity.UserDb
import space.active.taskmanager1c.data.local.db.TasksFromRemote.relations.AuthorAndTasks
import space.active.taskmanager1c.data.local.db.TasksFromRemote.relations.PerformerAndTasks
import space.active.taskmanager1c.data.local.db.TasksFromRemote.relations.TaskAndMessages
import space.active.taskmanager1c.data.remote.dto.TaskListDto
import space.active.taskmanager1c.data.utils.Request
import space.active.taskmanager1c.domain.utils.Resource

interface TasksFromRemoteDb {
//    val listTasksAndMessages: Flow<List<TaskAndMessages>>
    val listTasks: Flow<Request<List<TaskDb>>>
    val listUsers: Flow<Request<List<UserDb>>>
//    suspend fun getCoPerformerAndTasks(userId: String): Flow<List<TaskDb>>
    fun getTask(taskId: String): Flow<Request<TaskAndMessages>>
    fun getUser(userId: String): Flow<Request<UserDb>>
    fun updateListener(): Flow<Resource<String>>
//    suspend fun saveFromRemote(tasksRemote: TaskListDto, tasksLocal: List<TaskDb>)
//    suspend fun getAllUsersIdInTasks(): List<String>
}