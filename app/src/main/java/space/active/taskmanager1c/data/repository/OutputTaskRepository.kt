package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask

interface OutputTaskRepository {
    val outputTask: Flow<List<OutputTask>>
//    val outputTask: Flow<Request<List<OutputTask>>>
//    fun saveChangesAndTrySend(task: OutputTask): Flow<Request<Any>>
    suspend fun insertTask(outputTask: OutputTask)
    suspend fun getTasks(): List<OutputTask>
    suspend fun deleteTasks(outputTasks: List<OutputTask>)
}