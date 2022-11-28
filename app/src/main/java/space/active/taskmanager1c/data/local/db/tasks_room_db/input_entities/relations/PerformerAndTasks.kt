package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput

data class PerformerAndTasks(
    @Embedded
    val performer: UserInput,
    @Relation(
        parentColumn = "id",
        entityColumn = "performerId"
    )
    val tasks: TaskInput
)
