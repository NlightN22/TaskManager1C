package space.active.taskmanager1c.data.remote.model.messages_dto

data class TaskUserReadingFlagDTO(
    val flag: Boolean,
    val id: String,
    val user: String
)