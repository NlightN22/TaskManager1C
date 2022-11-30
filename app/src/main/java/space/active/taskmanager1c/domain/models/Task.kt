package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput

data class Task(
    val date: String,
    val description: String,
    val endDate: String,
    val id: String,
    val mainTaskId: String,
    val name: String,
    val number: String,
    val objName: String,
    val photos: List<Any>,
    val priority: String,
    val status: String,
    val users: UsersInTaskDomain,
) {
    companion object {
        fun fromTaskInput(taskInput: TaskInput): Task = Task(
            date = taskInput.date,
            description = taskInput.description,
            endDate = taskInput.endDate,
            id = taskInput.id,
            mainTaskId = taskInput.mainTaskId,
            name = taskInput.name,
            number = taskInput.number,
            objName = taskInput.objName,
            photos = taskInput.photos,
            priority = taskInput.priority,
            status = taskInput.status,
            users = UsersInTaskDomain.fromInputTask(taskInput.usersInTask),
        )

        fun fromTaskInputList(taskInputList: List<TaskInput>): List<Task> =
            taskInputList.map { fromTaskInput(it) }
    }
}

