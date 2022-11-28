package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserInput(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val name: String
)
