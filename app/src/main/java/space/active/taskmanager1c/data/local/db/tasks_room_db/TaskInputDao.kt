package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput

@Dao
interface TaskInputDao {

    @Query("SELECT * FROM TaskInput")
    fun getTasksFlow(): Flow<List<TaskInput>>

    @Query("SELECT * FROM TaskInput")
    fun getTasks(): List<TaskInput>

    @Query("SELECT * FROM TaskInput WHERE id = :taskId")
    fun getTaskFlow(taskId: String): Flow<TaskInput?>

    @Query("SELECT * FROM TaskInput WHERE id = :taskId")
    fun getTask(taskId: String): TaskInput?

    @Query("SELECT * FROM UserInput")
    fun getUsers(): List<UserInput>

    @Query("SELECT * FROM UserInput WHERE id = :userId")
    fun getUser(userId: String): UserInput?

    @Query("SELECT * FROM UserInput WHERE name = :userName")
    fun getUserByName(userName: String): UserInput?

    @Query("SELECT * FROM UserInput")
    fun getUsersFlow(): Flow<List<UserInput>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(taskInput: TaskInput)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(userInput: UserInput)

    @Query("DELETE FROM TaskInput")
    fun clearInputTable()

}