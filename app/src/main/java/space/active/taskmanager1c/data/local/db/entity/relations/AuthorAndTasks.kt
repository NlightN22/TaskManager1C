package space.active.taskmanager1c.data.local.db.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import space.active.taskmanager1c.data.local.db.entity.TaskDb
import space.active.taskmanager1c.data.local.db.entity.UserDb

data class AuthorAndTasks(
    @Embedded
    val author: UserDb,
    @Relation(
        parentColumn = "id",
        entityColumn = "authorId"
    )
    val tasks: TaskDb
)