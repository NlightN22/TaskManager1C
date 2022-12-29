package space.active.taskmanager1c.data.remote.model

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
    val name: String,
    val number: String,
    val objName: String,
    val observers: List<String>,
    val performerId: String,
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
            name = outputTask.taskInput.name,
            number = outputTask.taskInput.number,
            objName = outputTask.taskInput.objName,
            observers = outputTask.taskInput.usersInTask.observers,
            performerId = outputTask.taskInput.usersInTask.performerId,
            priority = outputTask.taskInput.priority,
            status = outputTask.taskInput.status
        )

        fun fromInputTask(inputTask: TaskInput): TaskDto = TaskDto(
            authorId = inputTask.usersInTask.authorId,
            coPerformers = inputTask.usersInTask.coPerformers,
            date = inputTask.date,
            description = inputTask.description,
            endDate = inputTask.endDate,
            id = inputTask.id,
            mainTaskId = inputTask.mainTaskId,
            name = inputTask.name,
            number = inputTask.number,
            objName = inputTask.objName,
            observers = inputTask.usersInTask.observers,
            performerId = inputTask.usersInTask.performerId,
            priority = inputTask.priority,
            status = inputTask.status
        )
    }
}
