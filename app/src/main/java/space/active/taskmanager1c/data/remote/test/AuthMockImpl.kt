package space.active.taskmanager1c.data.remote.test

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.domain.models.AuthUser
import space.active.taskmanager1c.domain.repository.Authorization

class AuthMockImpl: Authorization {

    // c49a0b62-c192-11e1-8a03-f46d0490adee Михайлов Олег Федорович

    private val mockkUser: AuthUser = AuthUser(
        tokenId = "",
        userId = "c49a0b62-c192-11e1-8a03-f46d0490adee",
        userName = "Михайлов Олег Федорович",
        pass = "test"
    )

    override fun auth(username: String, password: String): Flow<Request<AuthUser>> = flow {
        emit(PendingRequest())
        delay(1000)
        val res = mockkUser
        if (username != res.userName || password != res.pass) {
            emit(ErrorRequest(AuthException))
        } else {
            emit(SuccessRequest(res))
        }
    }
}