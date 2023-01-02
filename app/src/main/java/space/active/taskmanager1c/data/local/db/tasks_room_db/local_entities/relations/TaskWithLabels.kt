package space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.Label
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.TaskExtra

data class TaskWithLabels (
    @Embedded
    val task: TaskExtra,
    @Relation(
        parentColumn = "taskId",
        entityColumn = "labelName",
        associateBy = Junction(TaskExtraLabelCrossRef::class)
    )
    val labels: List<Label>
        )