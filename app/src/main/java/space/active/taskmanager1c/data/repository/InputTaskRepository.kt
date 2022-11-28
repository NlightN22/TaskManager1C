package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskAndMessages

interface InputTaskRepository  {
    val listTasks: Flow<List<TaskInput>>
    fun getTaskAndMessages(taskId: String): Flow<TaskAndMessages>
    // todo: sorted, ordered, filtered tasks
}