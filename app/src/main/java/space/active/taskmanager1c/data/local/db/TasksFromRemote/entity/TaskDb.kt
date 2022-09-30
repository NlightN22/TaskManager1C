package space.active.taskmanager1c.data.local.db.TasksFromRemote.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TaskDb(
    val authorId: String,
    val coPerformers: List<String>,
    val date: String,
    val description: String,
    val endDate: String,
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val mainTaskId: String,
    val name: String,
    val number: String,
    val objName: String,
    val observers: List<String>,
    val performerId: String,
    val photos: List<String>,
    val priority: String,
    val status: String
)
