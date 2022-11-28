package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.MessageInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput

data class TaskAndMessages (
    @Embedded
    val task: TaskInput,
    @Relation(
                parentColumn = "id", // in TaskInput
                entityColumn = "taskId" // in MessagesDb
            )
    val messages: List<MessageInput>
)