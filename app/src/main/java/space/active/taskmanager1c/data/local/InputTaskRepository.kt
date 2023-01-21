package space.active.taskmanager1c.data.local

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.SortField
import space.active.taskmanager1c.data.local.db.tasks_room_db.SortType
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskInputHandledWithUsers
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask

interface InputTaskRepository {
    // tasks todo delete
//    val listTaskFlow: Flow<List<TaskInputHandledWithUsers>>

    suspend fun getInputTasksCount(): Int

    // filtered
    fun sortedAll(sortField: SortField, sortType: SortType): Flow<List<TaskInputHandledWithUsers>>
    fun filteredIdo(
        myId: String,
        sortField: SortField,
        sortType: SortType
    ): Flow<List<TaskInputHandledWithUsers>>

    fun filteredIDelegate(
        myId: String,
        sortField: SortField,
        sortType: SortType
    ): Flow<List<TaskInputHandledWithUsers>>

    fun filteredIDidNtCheck(
        myId: String,
        sortField: SortField,
        sortType: SortType
    ): Flow<List<TaskInputHandledWithUsers>>

    fun filteredIObserve(
        myId: String,
        sortField: SortField,
        sortType: SortType
    ): Flow<List<TaskInputHandledWithUsers>>

    //todo delete
//    fun filteredIDidNtRead(
//        sortField: SortField,
//        sortType: SortType
//    ): Flow<List<TaskInputHandledWithUsers>>

    // ordered
    suspend fun getTasks(): List<TaskInputHandledWithUsers>
    fun getTaskFlow(taskId: String): Flow<TaskInputHandledWithUsers?>
    suspend fun getTask(taskId: String): TaskInputHandledWithUsers?
    suspend fun insertTasks(taskInputList: List<TaskInputHandledWithUsers>)
    suspend fun saveAndDelete(
        inputTask: TaskInputHandledWithUsers,
        outputTask: OutputTask,
        whoAmI: UserInput
    )

    // users
    val listUsersFlow: Flow<List<UserInput>>
    suspend fun getUser(userId: String): UserInput?
    suspend fun getUsers(): List<UserInput>
    suspend fun insertUsers(userInputList: List<UserInput>)
}