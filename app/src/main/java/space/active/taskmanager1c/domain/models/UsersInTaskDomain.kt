package space.active.taskmanager1c.domain.models

data class UsersInTaskDomain(
    val author: UserDomain,
    val performer: UserDomain,
    val coPerformers: List<UserDomain>,
    val observers: List<UserDomain>,
)