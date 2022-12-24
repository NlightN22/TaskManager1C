package space.active.taskmanager1c.data.remote.dto.messages_dto

data class TaskMessagesDTO(
    val messages: List<TaskMessageDTO>,
    val readingTime: String,
    val users: List<TaskMessagesUserDTO>
)