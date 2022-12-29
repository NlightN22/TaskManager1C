package space.active.taskmanager1c.data.remote.test

import space.active.taskmanager1c.data.remote.model.AuthUser

data class AuthUserMock(
    val tokenId: String,
    val userId: String,
    val userName: String,
    val pass: String
) {
    fun toAuthUser() = AuthUser (
        tokenId = tokenId,
        userId = userId,
        userName = userName
            )
}
