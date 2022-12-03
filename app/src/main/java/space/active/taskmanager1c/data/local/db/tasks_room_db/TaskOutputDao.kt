package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask

@Dao
interface TaskOutputDao {
    @Query("SELECT * FROM OutputTask")
    fun getOutputTasksFlow(): Flow<List<OutputTask>>

    @Query("SELECT * FROM OutputTask")
    fun getOutputTasks(): List<OutputTask>

    @Query("SELECT * FROM OutputTask WHERE id = :outputInputId")
    fun getOutputTaskFlow(outputInputId: String): Flow<OutputTask?>

    @Query("SELECT * FROM OutputTask WHERE id = :outputInputId")
    fun getOutputTask(outputInputId: String): OutputTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(outputTask: OutputTask)

    @Query("DELETE FROM OutputTask WHERE outputId = :outputId")
    fun deleteOutputTask(outputId: Int)
}