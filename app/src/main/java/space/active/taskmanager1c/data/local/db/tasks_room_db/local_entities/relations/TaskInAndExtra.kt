package space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.TaskExtra

data class TaskInAndExtra(
    @Embedded val taskIn: TaskInput,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val extra: TaskExtra
)
