package space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations

import androidx.room.Entity

@Entity(
    primaryKeys = ["taskId", "labelName"]
)
data class TaskExtraLabelCrossRef(
    val taskId: String,
    val labelName: String
)