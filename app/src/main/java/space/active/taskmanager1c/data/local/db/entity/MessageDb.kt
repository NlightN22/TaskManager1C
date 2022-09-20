package space.active.taskmanager1c.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MessageDb(
    val authorId: String,
    val date: String,
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val text: String,
    val taskId: String
)
