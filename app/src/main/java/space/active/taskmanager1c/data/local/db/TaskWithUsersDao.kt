package space.active.taskmanager1c.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import space.active.taskmanager1c.data.local.db.entity.MessageDb
import space.active.taskmanager1c.data.local.db.entity.TaskDb
import space.active.taskmanager1c.data.local.db.entity.UserDb
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

    @Query("SELECT * FROM TaskDb")
    suspend fun getTasks(): List<TaskDb>
    @Query("SELECT * FROM TaskDb WHERE :taskId")
    suspend fun getTask(taskId: String): TaskDb
    @Query("SELECT * FROM UserDb WHERE :userId")
    suspend fun getUser(userId: String): UserDb
    @Query("SELECT * FROM MessageDb WHERE :messageId")
    suspend fun getMessage(messageId: String): MessageDb

}