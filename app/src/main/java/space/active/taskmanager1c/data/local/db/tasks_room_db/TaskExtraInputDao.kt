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

@Dao
interface TaskExtraInputDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTaskInput(taskInput: TaskInput)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTaskExtra(taskExtra: TaskExtra)

    @Transaction
    suspend fun insertTaskInAndExtra(taskInAndExtra: TaskInAndExtra) {
        insertTaskInput(taskInAndExtra.taskIn)
        insertTaskExtra(taskInAndExtra.extra)
    }

    @Transaction
    @Query("SELECT * FROM TaskInput")
    fun taskInAndExtraListFlow(): Flow<List<TaskInAndExtra>>

    @Transaction
    @Query("SELECT * FROM TaskInput")
    fun taskInAndExtraList(): List<TaskInAndExtra>

    @Transaction
    @Query("SELECT * FROM TaskInput WHERE id = :taskId")
    fun getTaskFlow(taskId: String): Flow<TaskInAndExtra?>

    @Transaction
    @Query("SELECT * FROM TaskInput WHERE id = :taskId")
    suspend fun getTaskExtra(taskId: String): TaskInAndExtra?

    // Labels
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLabel(crossRef: TaskExtraLabelCrossRef)

    @Transaction
    @Query("SELECT * FROM Label WHERE labelName = :labelName")
    fun getLabelWithTasks(labelName: String): Flow<LabelWithTasks?>

    @Transaction
    @Query("SELECT * FROM TaskExtra WHERE taskId = :taskId")
    fun getTaskWithLabels(taskId: String): Flow<TaskWithLabels?>

}