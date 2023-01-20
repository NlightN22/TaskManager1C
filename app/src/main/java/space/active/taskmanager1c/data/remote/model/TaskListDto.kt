package space.active.taskmanager1c.data.remote.model

import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.embedded.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput

data class TaskListDto(
    val tasks: List<TaskDto> = emptyList(),
    val users: List<UserDto> = emptyList()
) {
    fun toTaskInputList(): List<TaskInput> = this.tasks.map { it.toTaskInput() }
    fun toUserInputList(): List<UserInput> = this.users.map { it.toUserDb() }
}