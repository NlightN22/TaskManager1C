package space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.domain.models.User


@Entity
data class TaskExtra(
    @PrimaryKey(autoGenerate = false)
    val taskId: String,
    @Embedded
    val whoIsInTask: WhoIsInTask,
    val unread: Boolean = false,
    val ok: Boolean = false,
    val cancel: Boolean = false
    )
{
    fun insertFromInput(taskInput: TaskInput, user: UserInput): TaskExtra {
        val taskEx = taskInput.toTaskExtra(user)
     return this.copy(
            taskId = taskEx.taskId,
            whoIsInTask = taskEx.whoIsInTask,
         ok = taskEx.ok,
         cancel = taskEx.cancel
        )
    }
}
