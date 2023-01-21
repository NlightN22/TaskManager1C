package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    primaryKeys = [
        "coPerformerId",
        "taskId"
    ],
    indices = [
        Index("taskId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = TaskInputHandled::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class CoPerformersInTask(
    val coPerformerId: String,
    val taskId: String
)
