package space.active.taskmanager1c.data.local.db

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.entity.MessageDb
import space.active.taskmanager1c.data.local.db.entity.TaskDb
import space.active.taskmanager1c.data.local.db.entity.UserDb
import space.active.taskmanager1c.data.local.db.entity.relations.AuthorAndTasks
import space.active.taskmanager1c.data.local.db.entity.relations.PerformerAndTasks
import space.active.taskmanager1c.data.local.db.entity.relations.TaskAndMessages
import space.active.taskmanager1c.data.remote.dto.TaskListDto

interface TasksFromRemoteDb {
    val listTasksAndMessages: Flow<List<TaskAndMessages>>
    val listUsers: Flow<List<UserDb>>
//    suspend fun getCoPerformerAndTasks(userId: String): Flow<List<TaskDb>>
    suspend fun getTask(taskId: String): Flow<TaskAndMessages>
    suspend fun getUser(userId: String): Flow<UserDb>
    suspend fun saveFromRemote(tasksRemote: TaskListDto, tasksLocal: List<TaskDb>)
//    suspend fun getAllUsersIdInTasks(): List<String>
}