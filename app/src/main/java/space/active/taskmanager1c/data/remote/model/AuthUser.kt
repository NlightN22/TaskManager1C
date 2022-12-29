package space.active.taskmanager1c.data.remote.model


data class AuthUser(
    val tokenId: String,
    val userId: String,
    val userName: String,
) {
    companion object {
        fun UserDto.toUserDomain(): AuthUser = AuthUser(
            tokenId = "",
            userId = this.id,
            userName = this.name ?: ""
        )
    }
}
