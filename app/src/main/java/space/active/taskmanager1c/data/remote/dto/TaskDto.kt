package space.active.taskmanager1c.data.remote.dto

data class TaskDto(
    val tasks: List<Task>,
    val users: List<User>
)