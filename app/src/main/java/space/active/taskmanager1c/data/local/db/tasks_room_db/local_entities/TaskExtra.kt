package space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class TaskExtra(
    @PrimaryKey(autoGenerate = false)
    val taskId: String,
    @Embedded
    val whoIsInTask: WhoIsInTask,
    val isRead: Boolean = false,
    )
