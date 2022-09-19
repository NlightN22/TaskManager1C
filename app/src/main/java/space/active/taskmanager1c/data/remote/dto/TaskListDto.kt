package space.active.taskmanager1c.data.remote.dto

data class TaskListDto(
    val tasks: List<TaskDto>,
    val users: List<UserDto>
)