package space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded

import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.domain.models.UserDomain
import space.active.taskmanager1c.domain.models.UsersInTaskDomain


data class UsersInTask(
    val authorId: String,
    val coPerformers: List<String>,
    val performerId: String,
    val observers: List<String>,
) {
    fun toUsersDomain(listUsers: List<UserInput>): UsersInTaskDomain = UsersInTaskDomain(
        author = listUsers.toUserDomain(authorId),
        performer = listUsers.toUserDomain(performerId),
        coPerformers = coPerformers.map { listUsers.toUserDomain(it) },
        observers = observers.map { listUsers.toUserDomain(it) }

    )

    private fun List<UserInput>.toUserDomain(id: String): UserDomain {
        return this.find { it.id == id }?.toUserDomain() ?: UserDomain( id = id, name = id)
    }
}