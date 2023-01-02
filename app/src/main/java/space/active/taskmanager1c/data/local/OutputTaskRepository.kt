package space.active.taskmanager1c.data.local

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask

interface OutputTaskRepository {
    val outputTaskList: Flow<List<OutputTask>>
    suspend fun insertTask(outputTask: OutputTask)
    suspend fun getTasks(): List<OutputTask>

    /**
     * Get null if we don't have sending tasks
     */
    fun getTaskFlow(taskInputId: String): Flow<OutputTask?>
    suspend fun getTask(taskInputId: String): OutputTask?
    suspend fun deleteTasks(outputTasks: List<OutputTask>)
    suspend fun deleteTask(outputTask: OutputTask)
}