package space.active.taskmanager1c.data.remote.dto

import space.active.taskmanager1c.data.local.db.entity.UserDb

data class UserDto(
    val id: String,
    val name: String
) {
    fun toUserDb(): UserDb {
        return UserDb(
            id = id,
            name = name
        )
    }
}