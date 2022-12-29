package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.UsersInTask


@Entity
data class TaskInput(
    val date: String,
    val description: String,
    val endDate: String,
    @PrimaryKey(autoGenerate = false)
    val id: String,
    val mainTaskId: String,
    val name: String,
    val number: String,
    val objName: String,
    val priority: String,
    val status: String,
    @Embedded val usersInTask: UsersInTask
)
