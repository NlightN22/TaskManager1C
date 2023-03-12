package space.active.taskmanager1c.data.remote.model

import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput

data class UserDto(
    val id: String,
    val name: String?
) {
    fun toUserDb(): UserInput {
        return UserInput(
            userId = id,
            userName = name?: ""
        )
    }
    fun toAuthUserDto(): AuthUserDto = AuthUserDto(
        tokenId = "",
        userId = this.id,
        userName = this.name ?: ""
    )
}