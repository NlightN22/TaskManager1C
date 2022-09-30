package space.active.taskmanager1c.data.local.db.TasksFromRemote.relations

import androidx.room.Embedded
import androidx.room.Relation
import space.active.taskmanager1c.data.local.db.TasksFromRemote.entity.TaskDb
import space.active.taskmanager1c.data.local.db.TasksFromRemote.entity.UserDb

data class PerformerAndTasks(
    @Embedded
    val performer: UserDb,
    @Relation(
        parentColumn = "id",
        entityColumn = "performerId"
    )
    val tasks: TaskDb
)
