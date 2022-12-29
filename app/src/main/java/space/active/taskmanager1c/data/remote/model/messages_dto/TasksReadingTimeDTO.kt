package space.active.taskmanager1c.data.remote.model.messages_dto

data class TasksReadingTimeDTO(
    val id: String,
    val lastMessageReadTime: String,
    val readingTime: String
)