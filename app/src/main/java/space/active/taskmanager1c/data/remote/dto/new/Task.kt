package space.active.taskmanager1c.data.remote.dto.new

data class Task(
    val authorId: String,
    val coPerformers: List<String>,
    val date: String,
    val description: String,
    val endDate: String,
    val id: String,
    val mainTaskId: String,
    val messages: List<Message>,
    val name: String,
    val number: String,
    val objName: String,
    val observers: List<Any>,
    val performerId: String,
    val photos: List<Any>,
    val priority: String,
    val status: String
)