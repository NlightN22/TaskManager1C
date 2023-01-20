package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInputHandled
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask

@Dao
interface TaskInputDao {

    @Query("SELECT * FROM TaskInputHandled")
    fun getTasksFlow(): Flow<List<TaskInputHandled>>

    @Query("SELECT * FROM TaskInputHandled")
    suspend fun getTasks(): List<TaskInputHandled>

    @Query("SELECT * FROM TaskInputHandled WHERE id = :taskId")
    fun getTaskFlow(taskId: String): Flow<TaskInputHandled?>

    @Query("SELECT * FROM TaskInputHandled WHERE id = :taskId")
    suspend fun getTask(taskId: String): TaskInputHandled?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(TaskInputHandled: TaskInputHandled)

    @Query("UPDATE TaskInputHandled SET unread = :unread WHERE id = :taskId ")
    fun updateReadingState(taskId: String, unread: Boolean)

    @Transaction
    suspend fun saveAndDelete(
        inputTask: TaskInput,
        outputTask: OutputTask,
        taskExtra: TaskInputHandled
    ) {
        insertTask(taskExtra)
        deleteOutputTask(outputTask.outputId)
    }

    @Query("DELETE FROM OutputTask WHERE outputId = :outputId")
    abstract suspend fun deleteOutputTask(outputId: Int)

    @Query("SELECT * FROM UserInput")
    fun getUsersFlow(): Flow<List<UserInput>>

    @Query("SELECT * FROM UserInput")
    fun getUsers(): List<UserInput>

    @Query("SELECT * FROM UserInput WHERE id = :userId")
    fun getUser(userId: String): UserInput?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(userInput: UserInput)
}