package space.active.taskmanager1c.data.remote.dto.messages_dto

data class TaskMessagesDTOTemp(
    val messages: TaskMessageDTO,
    val readingTime: String,
    val users: List<TaskMessagesUserDTO>
)