package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.UsersInTask

data class UsersInTaskDomain(
    val author: UserDomain,
    val performer: UserDomain,
    val coPerformers: List<UserDomain>,
    val observers: List<UserDomain>,
) {

    fun toTaskInput() = UsersInTask(
        authorId = this.author.id,
        performerId = this.performer.id,
        coPerformers = this.coPerformers.map { it.id },
        observers = this.observers.map { it.id },
    )
}