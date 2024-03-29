package space.active.taskmanager1c.data.local

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask

interface OutputTaskRepository {
    val outputTaskList: Flow<List<OutputTask>>
    suspend fun insertTask(outputTask: OutputTask)
    suspend fun getTasks(): List<OutputTask>

    /**
     * Get null if we don't have sending taskDomains
     */
    fun getTaskFlow(taskInputId: String): Flow<OutputTask?>
    suspend fun getTask(taskInputId: String): OutputTask?
    suspend fun deleteTasks(outputIdList: List<String>)
    suspend fun deleteTask(outputTask: OutputTask)
}