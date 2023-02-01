package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class TaskInputHandled(
    val date: String,
    val description: String,
    val endDate: String?,
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val mainTaskId: String?,
    val name: String,
    val number: String,
    val objName: String?,
    val priority: String,
    val status: String,
    val authorId: String,
    val performerId: String,
    val unreadTag: Boolean,
    val version: String, // Version of data to compare and write newest
    // extra values
    val isAuthor: Boolean,
    val isPerformer: Boolean,
    val ok: Boolean = false,
    val cancel: Boolean = false,
)
