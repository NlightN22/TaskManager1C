package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask

@Dao
interface TaskOutputDao {
    @Query("SELECT * FROM OutputTask")
    fun getAllOutputTasks(): Flow<List<OutputTask>>
    @Query("SELECT * FROM OutputTask WHERE outputId = :outputId")
    fun getOutputTask(outputId: String): Flow<OutputTask>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(outputTask: OutputTask)
    @Query("DELETE FROM OutputTask WHERE outputId = :outputId")
    fun deleteOutputTask(outputId: String)
}