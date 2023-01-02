package space.active.taskmanager1c.data.local

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput

interface InputTaskRepository {

    val listTaskFlow: Flow<List<TaskInput>>
    val listTasksRequest: Flow<Request<List<TaskInput>>>
    suspend fun getTasks(): List<TaskInput>
    fun getTaskFlow(taskId: String): Flow<TaskInput?>
    suspend fun getTask(taskId: String): TaskInput?
    suspend fun insertTask(taskInput: TaskInput)
    suspend fun insertTasks(taskInputList: List<TaskInput>)
    val listUsersFlow: Flow<List<UserInput>>
    suspend fun getUser(userId: String): UserInput?
    suspend fun getUserByName(username: String): UserInput?
    suspend fun insertUser(userInput: UserInput)
    suspend fun insertUsers(userInputList: List<UserInput>)
    suspend fun clearTable()
}