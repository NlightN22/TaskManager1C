package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.UsersInTask

data class UsersInTaskDomain(
    val author: User,
    val performer: User,
    val coPerformers: List<User>,
    val observers: List<User>,
) {

    fun toTaskInput() = UsersInTask(
        authorId = this.author.id,
        performerId = this.performer.id,
        coPerformers = this.coPerformers.map { it.toString() },
        observers = this.observers.map { it.toString() },
    )

    companion object {
//        fun fromInputTask(inputUsers: UserInput): UsersInTaskDomain = UsersInTaskDomain(
//            author = inputUsers.toUserDomain(),
//            performer = inTaskUsers.performerId,
//            coPerformers = inTaskUsers.coPerformers,
//            observers = inTaskUsers.observers,
//        )
    }
}