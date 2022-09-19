package space.active.taskmanager1c.data.remote.dto

data class TaskDto(
    val authorId: String,
    val coPerformers: List<String>,
    val date: String,
    val description: String,
    val endDate: String,
    val id: String,
    val mainTask: String,
    val mainTaskId: String,
    val messageDto: List<MessageDto>,
    val messages: List<Any>,
    val name: String,
    val number: String,
    val obj_name: String,
    val observers: List<String>,
    val performer: String,
    val performerId: String,
    val photos: List<String>,
    val priority: String,
    val status: String
)