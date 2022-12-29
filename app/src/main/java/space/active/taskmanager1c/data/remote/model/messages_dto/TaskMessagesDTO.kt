package space.active.taskmanager1c.data.remote.model.messages_dto

data class TaskMessagesDTO(
    val messages: List<TaskMessageDTO>,
    val readingTime: String,
    val users: List<TaskMessagesUserDTO>
)