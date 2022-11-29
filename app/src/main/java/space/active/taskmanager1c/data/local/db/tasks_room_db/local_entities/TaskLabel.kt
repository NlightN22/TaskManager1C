package space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities

//TODO: add label system to tasks
data class TaskLabel(
    val taskId: String,
    val isReadied: Boolean,
    val userLabelList: List<String>
    )
