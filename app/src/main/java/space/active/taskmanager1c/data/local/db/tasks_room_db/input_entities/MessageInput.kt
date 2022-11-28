package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MessageInput(
    val authorId: String,
    val date: String,
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val text: String?,
    val taskId: String
)
