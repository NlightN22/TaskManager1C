package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTasks

@Dao
interface TaskOutputDao {
    @Query("SELECT * FROM OutputTasks")
    fun getAllOutputTasks(): Flow<List<OutputTasks>>
    @Query("SELECT * FROM OutputTasks WHERE taskId = :taskId")
    fun getOutputTask(taskId: String): Flow<OutputTasks>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveChanges(outputTasks: OutputTasks)
    @Query("DELETE FROM OutputTasks WHERE taskId = :taskId")
    fun deleteOutputTask(taskId: String)
}