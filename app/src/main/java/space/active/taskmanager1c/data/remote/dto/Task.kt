package space.active.taskmanager1c.data.remote.dto

data class Task(
    val authorId: String,
    val coPerformers: List<String>,
    val `data`: String,
    val date: String,
    val description: String,
    val endDate: String,
    val id: String,
    val mainTask: String,
    val mainTaskId: String,
    val message: List<Message>,
    val messages: List<Any>,
    val name: String,
    val number: String,
    val `object`: String,
    val observers: List<String>,
    val performer: String,
    val performerId: String,
    val photos: List<String>,
    val priority: String,
    val status: String
)