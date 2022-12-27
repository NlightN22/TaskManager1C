package space.active.taskmanager1c.data.remote.dto.messages_dto

data class TaskUserReadingFlagDTO(
    val flag: Boolean,
    val id: String,
    val user: String
)