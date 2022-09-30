package space.active.taskmanager1c.data.remote.dto

import space.active.taskmanager1c.data.local.db.TasksFromRemote.entity.TaskDb

data class TaskDto(
    val authorId: String,
    val coPerformers: List<String>,
    val date: String,
    val description: String?,
    val endDate: String?,
    val id: String,
    val mainTaskId: String?,
    val messageDto: List<MessageDto>,
    val name: String?,
    val number: String?,
    val objName: String?,
    val observers: List<String>,
    val performerId: String,
    val photos: List<String>,
    val priority: String,
    val status: String
) {
    fun toTaskDb(): TaskDb {
        return TaskDb(
            authorId = authorId,
            coPerformers = coPerformers,
            date = date,
            description = description ?: "",
            endDate = endDate ?: "",
            id = id,
            mainTaskId = mainTaskId ?: "",
            name = name ?: "",
            number = number ?: "",
            objName = objName ?: "",
            observers = observers,
            performerId = performerId,
            photos = photos,
            priority = priority,
            status = status,
        )
    }
}
