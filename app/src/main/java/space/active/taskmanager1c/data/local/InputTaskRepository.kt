package space.active.taskmanager1c.data.local

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.GetSortInt
import space.active.taskmanager1c.data.local.db.tasks_room_db.SortField
import space.active.taskmanager1c.data.local.db.tasks_room_db.SortType
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInputHandled
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask

interface InputTaskRepository {
    // tasks
    val listTaskFlow: Flow<List<TaskInputHandled>>
    // filtered
    fun sortedAll(sortField: SortField, sortType: SortType): Flow<List<TaskInputHandled>>
    fun filteredIdo(myId: String, sortField: SortField, sortType: SortType): Flow<List<TaskInputHandled>>
    fun filteredIDelegate(myId: String, sortField: SortField, sortType: SortType): Flow<List<TaskInputHandled>>
    fun filteredIDidNtCheck(myId: String, sortField: SortField, sortType: SortType): Flow<List<TaskInputHandled>>
    fun filteredIObserve(myId: String, sortField: SortField, sortType: SortType): Flow<List<TaskInputHandled>>
    fun filteredIDidNtRead(sortField: SortField, sortType: SortType): Flow<List<TaskInputHandled>>
    // ordered
    suspend fun getTasks(): List<TaskInputHandled>
    fun getTaskFlow(taskId: String): Flow<TaskInputHandled?>
    suspend fun getTask(taskId: String): TaskInputHandled?
    suspend fun insertTasks(taskInputList: List<TaskInput>, whoAmI: UserInput)
    suspend fun updateReading(taskId: String, unread: Boolean)
    suspend fun saveAndDelete(inputTask: TaskInput, outputTask: OutputTask, whoAmI: UserInput)
    // users
    val listUsersFlow: Flow<List<UserInput>>
    suspend fun getUser(userId: String): UserInput?
    suspend fun getUsers(): List<UserInput>
    suspend fun insertUsers(userInputList: List<UserInput>)
}