package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded


data class UsersInTask(
    val authorId: String,
    val coPerformers: List<String>,
    val performerId: String,
    val observers: List<String>,
)