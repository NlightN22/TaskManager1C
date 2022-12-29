package space.active.taskmanager1c.data.remote.test

import space.active.taskmanager1c.data.remote.model.AuthUserDto

data class AuthUserMock(
    val tokenId: String,
    val userId: String,
    val userName: String,
    val pass: String
) {
    fun toAuthUser() = AuthUserDto (
        tokenId = tokenId,
        userId = userId,
        userName = userName
            )
}
