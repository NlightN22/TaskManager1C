package space.active.taskmanager1c.data.remote.model.messages_dto

data class TaskMessageDTO(
    val authorId: String,
    val date: String,
    val id: String,
    val text: String
)