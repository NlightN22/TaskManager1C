package space.active.taskmanager1c.data.remote.model


data class AuthUserDto(
    val tokenId: String,
    val userId: String,
    val userName: String,
) {
    companion object {
        fun UserDto.toUserDomain(): AuthUserDto = AuthUserDto(
            tokenId = "",
            userId = this.id,
            userName = this.name ?: ""
        )
    }
}
