package space.active.taskmanager1c.data.remote.dto

import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput

data class TaskDto(
    val authorId: String,
    val coPerformers: List<String>,
    val date: String,
    val description: String,
    val endDate: String,
    val id: String,
    val mainTaskId: String,
    val messages: List<MessageDto>,
    val name: String,
    val number: String,
    val objName: String,
    val observers: List<String>,
    val performerId: String,
    val photos: List<String>,
    val priority: String,
    val status: String
) {
    fun toTaskDb(): TaskInput {
        return TaskInput(
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
            observers = observers ?: emptyList(),
            performerId = performerId,
            photos = photos?: emptyList(),
            priority = priority,
            status = status,
        )
    }
}
