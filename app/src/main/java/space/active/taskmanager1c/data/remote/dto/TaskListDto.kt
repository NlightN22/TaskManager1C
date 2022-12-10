package space.active.taskmanager1c.data.remote.dto

import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.remote.dto.for_update.User

data class TaskListDto(
    val tasks: List<TaskDto> = emptyList(),
    val users: List<UserDto> = emptyList()
) {
    fun toTaskInputList(): List<TaskInput> = this.tasks.map { it.toTaskInput() }
    fun toUserInputList(): List<UserInput> = this.users.map { it.toUserDb() }
}