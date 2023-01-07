package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.data.remote.TaskApi
import space.active.taskmanager1c.data.remote.model.AuthBasicDto
import space.active.taskmanager1c.data.remote.model.AuthUserDto
import space.active.taskmanager1c.data.remote.model.AuthUserDto.Companion.toUserDomain
import space.active.taskmanager1c.domain.repository.Authorization
import javax.inject.Inject

class AuthorizationImpl @Inject constructor(
    private val taskApi: TaskApi
): Authorization {

    override fun auth(username: String, password: String, serverAddress: String): Flow<Request<AuthUserDto>> = flow {
            emit(PendingRequest())
            emit(SuccessRequest(taskApi.authUser(AuthBasicDto(username,password)).toUserDomain()))
    }
}