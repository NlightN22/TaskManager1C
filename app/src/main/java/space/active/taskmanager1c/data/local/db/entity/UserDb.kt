package space.active.taskmanager1c.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserDb(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val name: String
)
