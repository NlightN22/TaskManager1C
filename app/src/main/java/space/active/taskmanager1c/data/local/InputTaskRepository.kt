package space.active.taskmanager1c.data.local

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.FilterType
import space.active.taskmanager1c.data.local.db.tasks_room_db.SortField
import space.active.taskmanager1c.data.local.db.tasks_room_db.SortType
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.ReadingTimesTaskEntity
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskInputHandledWithUsers
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask

interface InputTaskRepository {

    suspend fun getInputTasksCount(): Int

    fun sortedQuery(
        myId: String,
        filterType: FilterType,
        sortField: SortField,
        sortType: SortType
    ): Flow<List<TaskInputHandledWithUsers>>

    suspend fun getTasks(): List<TaskInputHandledWithUsers>
    fun getTaskFlow(taskId: String): Flow<TaskInputHandledWithUsers?>
    fun getInnerTasks(taskId: String): Flow<List<TaskInputHandledWithUsers>>
    suspend fun getTask(taskId: String): TaskInputHandledWithUsers?
    /**
     * return count of insert tasks and deleted tasks
     */
    suspend fun insertTasks(taskInputList: List<TaskInputHandledWithUsers>) : Pair<Int,Int>
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
    // readings
    /**
     * return count of reading state
     */
    suspend fun updateReadingStates(readingTimes: List<ReadingTimesTaskEntity>): Int
    fun getUnreadIds(): Flow<List<String>>
    suspend fun setUnreadTag(taskId: String, version: Int, unreadTag: Boolean)
}