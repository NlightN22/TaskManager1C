package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.MessageInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskAndMessages

@Dao
interface TaskInputDao {

    @Transaction
    @Query("SELECT * FROM TaskInput WHERE id = :taskId")
    fun getTaskAndMessages(taskId: String): Flow<TaskAndMessages>
    @Query("SELECT * FROM TaskInput")
    fun getTasksFlow(): Flow<List<TaskInput>>
    @Query("SELECT * FROM TaskInput")
    fun getTasks(): List<TaskInput>
    @Query("SELECT * FROM TaskInput WHERE id = :taskId")
    fun getTask(taskId: String): TaskInput
    @Query("SELECT * FROM UserInput")
    fun getUsers(): List<UserInput>
    @Query("SELECT * FROM UserInput WHERE id = :userId")
    fun getUser(userId: String): UserInput
    @Query("SELECT * FROM MessageInput WHERE id = :messageId")
    fun getMessage(messageId: String): MessageInput
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(taskInput: TaskInput)

}