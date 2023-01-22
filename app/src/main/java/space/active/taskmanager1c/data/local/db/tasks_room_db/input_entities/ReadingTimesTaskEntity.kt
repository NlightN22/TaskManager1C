package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReadingTimesTaskEntity(
    @PrimaryKey(autoGenerate = false)
    val mainTaskId: String,
    val lastMessageTime: String,
    val taskReadingTime: String,
    val isUnread: Boolean = false,
)
