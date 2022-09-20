package space.active.taskmanager1c.data.local.db

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.entity.TaskDb
import space.active.taskmanager1c.data.local.db.entity.UserDb
import space.active.taskmanager1c.data.local.db.entity.relations.TaskAndMessages
import space.active.taskmanager1c.data.remote.dto.MessageDto
import space.active.taskmanager1c.data.remote.dto.TaskDto
import space.active.taskmanager1c.data.remote.dto.TaskListDto
import space.active.taskmanager1c.data.remote.dto.UserDto

class TasksFromRemoteDbImpl(
    private val dao: TaskWithUsersDao,
) : TasksFromRemoteDb {

    val msg = Channel<String>()

    override val listTasksAndMessages: Flow<List<TaskAndMessages>>
        get() = TODO("Not yet implemented")
    override val listUsers: Flow<List<UserDb>>
        get() = TODO("Not yet implemented")

//    override suspend fun getCoPerformerAndTasks(userId: String): Flow<List<TaskDb>> = flow {
//        val tasks = dao.getTasks()
//        val coPerformersTasks = tasks.filter { task ->
//            task.coPerformers.any { it == userId }
//        }
//        emit(coPerformersTasks)
//    }

    override suspend fun getTask(taskId: String): Flow<TaskAndMessages> {
        TODO("Not yet implemented")
    }

    override suspend fun getUser(userId: String): Flow<UserDb> {
        TODO("Not yet implemented")
    }

    override suspend fun saveFromRemote(tasksRemote: TaskListDto, tasksLocal: List<TaskDb>) {
        // Load all Task from Db
        val dbTasksId = dao.getTasks().map { it.id }
        if (dbTasksId.isNotEmpty()) {
            // Find and delete not input tasks
            dbTasksId.forEach { localId ->
                if (tasksRemote.tasks.map { it.id }.notContainsId(localId)) {
                    dao.deleteTask(localId)
                }
            }
            // Find and delete not contained users
            val allUsers = getAllUsersIdInTasks()
            allUsers.forEach { userId->
                if (tasksRemote.users.map { it.id }.notContainsId(userId)) {
                    dao.deleteUser(userId)
                }
            }
        }
        // Save input tasks
        saveTasks(tasksRemote.tasks)
        saveUsers(tasksRemote.users)
    }

    private suspend fun getAllUsersIdInTasks(): List<String> {
        val tasks = dao.getTasks()
        val allUsers: List<String> =
            tasks.map { it.authorId } + tasks.map { it.performer } + tasks.flatMap { it.coPerformers }
        return allUsers
    }

    private fun List<String>.notContainsId(localId: String): Boolean {
        if (localId !in this) {
            return true
        }
        return false
    }

    private suspend fun saveTasks(tasksDto: List<TaskDto>) {
        dao.insertTask(tasksDto.map { it.toTaskDb() })
        tasksDto.forEach { task ->
            saveMessages(task.messageDto, taskId = task.id)
        }
    }

    private suspend fun saveUsers(users: List<UserDto>) {
        dao.insertUser(users.map { it.toUserDb() })
    }

    private suspend fun saveMessages(messages: List<MessageDto>, taskId: String) {
        dao.insertMessage(messages.map { it.toMessageDb(taskId) })
    }
}