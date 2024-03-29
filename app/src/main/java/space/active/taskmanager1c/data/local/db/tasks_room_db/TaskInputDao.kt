package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.*
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskInputHandledWithUsers
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.domain.use_case.SetTaskUnreadTag

@Dao
interface TaskInputDao {

    @Query("SELECT COUNT(id) FROM TaskInputHandled")
    suspend fun getInputCount(): Int

    @Query("SELECT TaskInputHandled.id FROM TaskInputHandled " +
            "LEFT JOIN InputTaskId " +
            "ON TaskInputHandled.id = InputTaskId.inputId " +
            "WHERE InputTaskId.inputId IS NULL")
    suspend fun getIdsNotInList(): List<String>

    @Query("DELETE FROM InputTaskId")
    suspend fun clearInputTaskId()

    @Insert(entity = InputTaskId::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInputTaskId(inputIds: List<InputTaskId>)

    @Transaction
    suspend fun clearAndInsertNewIds(listIds: List<String>) {
        clearInputTaskId()
        insertInputTaskId(listIds.map { InputTaskId(inputId = it) })
    }

    @RawQuery(observedEntities = [
        TaskInputHandled::class,
        CoPerformersInTask::class,
        ObserversInTask::class,
        ReadingTimesTaskEntity::class
    ])
    fun getSortedTaskQuery(query: SupportSQLiteQuery): Flow<List<TaskInputHandledWithUsers>>

    @Transaction
    @Query("SELECT * FROM TaskInputHandled")
    fun getTasksFlow(): Flow<List<TaskInputHandledWithUsers>>

    @Transaction
    @Query("SELECT * FROM TaskInputHandled WHERE mainTaskId = :taskId")
    fun getInnerTasksFlow(taskId: String): Flow<List<TaskInputHandledWithUsers>>

    @Transaction
    @Query("SELECT * FROM TaskInputHandled")
    suspend fun getTasks(): List<TaskInputHandledWithUsers>

    @Transaction
    @Query("SELECT * FROM TaskInputHandled WHERE id = :taskId")
    fun getTaskFlow(taskId: String): Flow<TaskInputHandledWithUsers?>

    @Transaction
    @Query("SELECT * FROM TaskInputHandled WHERE id = :taskId")
    suspend fun getTask(taskId: String): TaskInputHandledWithUsers?

    @Transaction
    suspend fun insertTask(taskInputHandled: TaskInputHandledWithUsers) {
        insertTaskInputHandled(taskInputHandled.taskInput)
        insertCoPerformersInTask(taskInputHandled.coPerformers)
        insertObserversInTask(taskInputHandled.observers)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTaskInputHandled(taskInputHandled: TaskInputHandled)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertCoPerformersInTask(coPerformersInTask: List<CoPerformersInTask>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertObserversInTask(observersInTask: List<ObserversInTask>)


    @Transaction
    suspend fun saveAndDelete(
        inputTask: TaskInputHandledWithUsers,
        outputTask: OutputTask,
    ) {
        insertTask(inputTask)
        deleteOutputTask(outputTask.outputId)
    }

    @Query("DELETE FROM TaskInputHandled WHERE id = :taskId")
    abstract fun deleteTaskInputHandled(taskId: String)

    @Query("DELETE FROM CoPerformersInTask WHERE taskId = :taskId")
    abstract fun deleteCoPerformersInTask(taskId: String)

    @Query("DELETE FROM ObserversInTask WHERE taskId = :taskId")
    abstract fun deleteObserversInTask(taskId: String)

    @Transaction
    suspend fun deleteTask(taskId: String) {
        deleteTaskInputHandled(taskId)
        deleteCoPerformersInTask(taskId)
        deleteObserversInTask(taskId)
    }

    @Query("DELETE FROM OutputTask WHERE outputId = :outputId")
    suspend fun deleteOutputTask(outputId: Int)

    @Query("SELECT * FROM UserInput")
    fun getUsersFlow(): Flow<List<UserInput>>

    @Query("SELECT * FROM UserInput")
    fun getUsers(): List<UserInput>

    @Query("SELECT * FROM UserInput WHERE userId = :userId")
    fun getUser(userId: String): UserInput?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(userInput: UserInput)

    @Query("UPDATE TaskInputHandled SET unreadTag = :unreadTag, version = :version WHERE id = :taskId")
    suspend fun updateUnreadTag(taskId: String, version: Int, unreadTag: Boolean)
}