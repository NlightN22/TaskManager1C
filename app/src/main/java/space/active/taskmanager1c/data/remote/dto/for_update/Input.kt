package space.active.taskmanager1c.data.remote.dto.for_update

data class Input(
    val tasks: List<Task>,
    val users: List<User>
)