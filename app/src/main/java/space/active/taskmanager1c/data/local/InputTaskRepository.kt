package space.active.taskmanager1c.data.local

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.Label
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.LabelWithTasks
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.TaskInAndExtra
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.TaskWithLabels

interface InputTaskRepository {
    // tasks
    val listTaskFlow: Flow<List<TaskInAndExtra>>
    suspend fun getTasks(): List<TaskInAndExtra>
    fun getTaskFlow(taskId: String): Flow<TaskInAndExtra?>
    suspend fun getTask(taskId: String): TaskInAndExtra?
    suspend fun insertTask(taskInput: TaskInput, whoAmI: UserInput)
    suspend fun insertTasks(taskInputList: List<TaskInput>, whoAmI: UserInput)
    // users
    val listUsersFlow: Flow<List<UserInput>>
    suspend fun getUser(userId: String): UserInput?
    suspend fun insertUser(userInput: UserInput)
    suspend fun insertUsers(userInputList: List<UserInput>)
    // labels
    fun taskWithLabels(taskId: String): Flow<TaskWithLabels?>
    fun labelWithTasks(label: Label): Flow<LabelWithTasks?>
    suspend fun insertLabel(taskId: String, label: Label)
}