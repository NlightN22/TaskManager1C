package space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    primaryKeys = ["taskId", "labelName"]
)
data class TaskExtraLabelCrossRef(
    @ColumnInfo(index = true)
    val taskId: String,
    @ColumnInfo(index = true)
    val labelName: String
)