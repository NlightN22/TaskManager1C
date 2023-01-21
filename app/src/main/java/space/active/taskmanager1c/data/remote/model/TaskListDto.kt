package space.active.taskmanager1c.data.remote.model

import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskInputHandledWithUsers

data class TaskListDto(
    val tasks: List<TaskDto> = emptyList(),
    val users: List<UserDto> = emptyList()
) {
    fun toTaskInputList(myId: String): List<TaskInputHandledWithUsers> =
        this.tasks.map { it.toTaskInputHandledWithUsers(myId) }

    fun toUserInputList(): List<UserInput> = this.users.map { it.toUserDb() }
}