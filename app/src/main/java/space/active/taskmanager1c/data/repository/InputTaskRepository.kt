package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskAndMessages

interface InputTaskRepository  {
    val listTasks: Flow<Request<List<TaskInput>>>
    fun getTaskAndMessages(taskId: String): Flow<TaskAndMessages>
    suspend fun getTasks(): List<TaskInput>
    suspend fun insertTask(taskInput: TaskInput)
    suspend fun insertTasks(taskInputList: List<TaskInput>)
    // todo: sorted, ordered, filtered tasks
}