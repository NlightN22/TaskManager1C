package space.active.taskmanager1c.data.local.db.TasksFromRemote

import androidx.room.*
import space.active.taskmanager1c.data.local.db.TasksFromRemote.entity.MessageDb
import space.active.taskmanager1c.data.local.db.TasksFromRemote.entity.TaskDb
import space.active.taskmanager1c.data.local.db.TasksFromRemote.entity.UserDb
import space.active.taskmanager1c.data.local.db.TasksFromRemote.relations.TaskAndMessages
import space.active.taskmanager1c.data.remote.dto.UserDto

@Dao
interface TaskWithUsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: List<TaskDb>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: List<UserDb>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: List<MessageDb>)

    @Query("DELETE FROM TaskDb WHERE :taskId")
    suspend fun deleteTask(taskId: String)
    @Query("DELETE FROM UserDb WHERE :userId")
    suspend fun deleteUser(userId: String)
    @Query("DELETE FROM MessageDb WHERE :messageId")
    suspend fun deleteMessage(messageId: String)

    @Transaction
    @Query("SELECT * FROM TaskDb WHERE id = :taskId")
    suspend fun getTaskAndMessages(taskId: String): TaskAndMessages
    @Query("SELECT * FROM TaskDb")
    suspend fun getTasks(): List<TaskDb>
    @Query("SELECT * FROM TaskDb WHERE id = :taskId")
    suspend fun getTask(taskId: String): TaskDb
    @Query("SELECT * FROM UserDb")
    suspend fun getUsers(): List<UserDb>
    @Query("SELECT * FROM UserDb WHERE id = :userId")
    suspend fun getUser(userId: String): UserDb
    @Query("SELECT * FROM MessageDb WHERE id = :messageId")
    suspend fun getMessage(messageId: String): MessageDb

}