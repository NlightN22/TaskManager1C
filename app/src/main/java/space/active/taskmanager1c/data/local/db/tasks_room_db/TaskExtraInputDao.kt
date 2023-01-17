package space.active.taskmanager1c.data.local.db.tasks_room_db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.TaskExtra
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.LabelWithTasks
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.TaskExtraLabelCrossRef
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.TaskInAndExtra
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations.TaskWithLabels
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask

@Dao
interface TaskExtraInputDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTaskInput(taskInput: TaskInput)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTaskExtra(taskExtra: TaskExtra)

    @Query("SELECT * FROM TaskInput WHERE id = :taskId")
    suspend fun getInput(taskId: String): TaskInput?

    @Query("SELECT * FROM TaskExtra WHERE taskId = :taskId")
    suspend fun getExtra(taskId: String): TaskExtra?

    @Transaction
    suspend fun insertTaskInAndExtra(taskExtra: TaskExtra, taskInput: TaskInput) {
        insertTaskInput(taskInput)
        insertTaskExtra(taskExtra)
    }

    @Query("UPDATE TaskExtra SET unread = :unread WHERE taskId = :taskId ")
    suspend fun updateIsReading(taskId: String, unread: Boolean)

    @Transaction
    @Query("SELECT * FROM TaskInput")
    fun taskInAndExtraListFlow(): Flow<List<TaskInAndExtra>>

    @Transaction
    @Query("SELECT * FROM TaskInput")
    suspend fun taskInAndExtraList(): List<TaskInAndExtra>

    @Transaction
    @Query("SELECT * FROM TaskInput WHERE id = :taskId")
    fun getTaskFlow(taskId: String): Flow<TaskInAndExtra?>

    @Transaction
    @Query("SELECT * FROM TaskInput WHERE id = :taskId")
    suspend fun getTaskInAndExtra(taskId: String): TaskInAndExtra?

    @Query("SELECT * FROM TaskExtra WHERE taskId = :taskId")
    suspend fun getTaskExtra(taskId: String): TaskExtra?

    // Labels
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabel(crossRef: TaskExtraLabelCrossRef)

    @Transaction
    @Query("SELECT * FROM Label WHERE labelName = :labelName")
    fun getLabelWithTasks(labelName: String): Flow<LabelWithTasks?>

    @Transaction
    @Query("SELECT * FROM TaskExtra WHERE taskId = :taskId")
    fun getTaskWithLabels(taskId: String): Flow<TaskWithLabels?>

    @Transaction
    suspend fun saveAndDelete(inputTask: TaskInput, outputTask: OutputTask, taskExtra: TaskExtra) {
        insertTaskInput(inputTask)
        insertTaskExtra(taskExtra)
        deleteOutputTask(outputTask.outputId)
    }

    @Query("DELETE FROM OutputTask WHERE outputId = :outputId")
    abstract suspend fun deleteOutputTask(outputId: Int)

}