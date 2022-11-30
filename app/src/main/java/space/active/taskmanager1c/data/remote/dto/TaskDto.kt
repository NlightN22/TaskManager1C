package space.active.taskmanager1c.data.remote.dto

import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.UsersInTask
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask

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
    fun toTaskInput(): TaskInput = TaskInput(
            usersInTask = UsersInTask(
                authorId = authorId,
                coPerformers = coPerformers ?: emptyList(),
                observers = observers ?: emptyList(),
                performerId = performerId
            ),
            date = date,
            description = description ?: "",
            endDate = endDate ?: "",
            id = id,
            mainTaskId = mainTaskId ?: "",
            name = name ?: "",
            number = number ?: "",
            objName = objName ?: "",
            photos = photos?: emptyList(),
            priority = priority,
            status = status,
        )

    companion object {
        fun fromOutputTask(outputTask: OutputTask): TaskDto = TaskDto(
            authorId = outputTask.taskInput.usersInTask.authorId,
            coPerformers = outputTask.taskInput.usersInTask.coPerformers,
            date = outputTask.taskInput.date,
            description = outputTask.taskInput.description,
            endDate = outputTask.taskInput.endDate,
            id = outputTask.taskInput.id,
            mainTaskId = outputTask.taskInput.mainTaskId,
            messages = emptyList(), // TODO separate message to another request
            name = outputTask.taskInput.name,
            number = outputTask.taskInput.number,
            objName = outputTask.taskInput.objName,
            observers = outputTask.taskInput.usersInTask.observers,
            performerId = outputTask.taskInput.usersInTask.performerId,
            photos = emptyList(), // TODO separate photos to another request
            priority = outputTask.taskInput.priority,
            status = outputTask.taskInput.status
        )
    }
}
