package space.active.taskmanager1c.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.data.remote.model.AuthUser
import space.active.taskmanager1c.data.remote.model.AuthUser.Companion.toUserDomain
import space.active.taskmanager1c.domain.repository.Authorization
import javax.inject.Inject

class AuthorizationImpl @Inject constructor(
    private val taskApi: TaskApi
): Authorization {


    override fun auth(username: String, password: String): Flow<Request<AuthUser>> = flow {
            emit(PendingRequest())
            emit(SuccessRequest(taskApi.authUser().toUserDomain()))
    }
}