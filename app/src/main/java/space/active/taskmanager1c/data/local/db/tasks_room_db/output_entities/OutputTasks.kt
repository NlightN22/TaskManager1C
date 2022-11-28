package space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput

@Entity
data class OutputTasks(
    @PrimaryKey(autoGenerate = false)
    val taskId: String,
    val key: String,
    val value: String?,
)
{
    fun toDtoParamsName(taskInput: TaskInput, parameterName: ParameterName): String {
        TODO("get name from model of TaskInput")
    }
}
