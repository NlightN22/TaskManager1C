package space.active.taskmanager1c.data.remote.dto

data class TaskListDto(
    val tasks: List<TaskDto> = emptyList(),
    val users: List<UserDto> = emptyList()
)