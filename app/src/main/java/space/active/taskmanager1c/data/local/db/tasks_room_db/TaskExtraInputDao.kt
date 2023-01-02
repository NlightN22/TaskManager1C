package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.TaskExtra
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.TaskInAndExtra

@Dao
interface TaskExtraInputDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskInput(taskInput: TaskInput)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskExtra(taskExtra: TaskExtra)

    @Transaction
    @Query("SELECT * FROM TaskInput")
    fun taskInAndExtraList(): Flow<List<TaskInAndExtra>>

    @Transaction
    @Query("SELECT * FROM TaskInput WHERE id = :taskId")
    fun getTaskExtra(taskId: String): Flow<TaskInAndExtra>

}