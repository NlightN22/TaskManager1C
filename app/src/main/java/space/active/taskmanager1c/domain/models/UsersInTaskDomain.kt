package space.active.taskmanager1c.domain.models

data class UsersInTaskDomain(
    val author: UserDomain,
    val performer: UserDomain,
    val coPerformers: List<UserDomain>,
    val observers: List<UserDomain>,
) {

    //todo delete
//    fun toTaskInput() = UsersInTask(
//        authorId = this.author.id,
//        performerId = this.performer.id,
//        coPerformers = this.coPerformers.map { it.id },
//        observers = this.observers.map { it.id },
//    )
}