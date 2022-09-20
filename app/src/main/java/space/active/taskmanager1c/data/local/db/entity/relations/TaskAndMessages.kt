package space.active.taskmanager1c.data.local.db.entity.relations

import androidx.room.Embedded
import androidx.room.Relation
import space.active.taskmanager1c.data.local.db.entity.MessageDb
import space.active.taskmanager1c.data.local.db.entity.TaskDb

data class TaskAndMessages (
    @Embedded
    val task: TaskDb,
            @Relation(
                parentColumn = "id", // in TaskDb
                entityColumn = "taskId" // in MessagesDb
            )
    val messages: List<MessageDb>
)