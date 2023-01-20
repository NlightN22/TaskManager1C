package space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.TaskInput

@Entity
data class OutputTask(
    @PrimaryKey(autoGenerate = true)
    val outputId: Int = 0,
    val newTask: Boolean = false,
    @Embedded
    val taskInput: TaskInput,
)
{
    companion object {
        /**
         * Return list of TaskInput from List of OutputTask
         */
        fun List<OutputTask>.toListTaskInput(): List<TaskInput> {
            return this.map { it.taskInput }
        }
    }
}
