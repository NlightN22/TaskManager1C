package space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Label (
    @PrimaryKey(autoGenerate = false)
    val labelName: String
)