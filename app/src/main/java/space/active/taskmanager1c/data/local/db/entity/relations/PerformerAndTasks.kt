package space.active.taskmanager1c.data.local.db.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import space.active.taskmanager1c.data.local.db.entity.TaskDb
import space.active.taskmanager1c.data.local.db.entity.UserDb

data class PerformerAndTasks(
    @Embedded
    val performer: UserDb,
    @Relation(
        parentColumn = "id",
        entityColumn = "performerId"
    )
    val tasks: TaskDb
)
