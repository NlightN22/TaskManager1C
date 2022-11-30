package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.UsersInTask

data class UsersInTaskDomain(
    val authorId: String,
    val performerId: String,
    val coPerformers: List<String>,
    val observers: List<String>,
) {
    companion object {
        fun fromInputTask(inTaskUsers: UsersInTask): UsersInTaskDomain = UsersInTaskDomain(
            authorId = inTaskUsers.authorId,
            performerId = inTaskUsers.performerId,
            coPerformers = inTaskUsers.coPerformers,
            observers = inTaskUsers.observers,
        )
    }
}