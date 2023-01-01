package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.User
import space.active.taskmanager1c.domain.models.UserSettings
import javax.inject.Inject

class GetUserSettingsFromDataStore @Inject constructor(
    private val exceptionHandler: ExceptionHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    val mockUser = User (
        "c49a0b62-c192-11e1-8a03-f46d0490adee",
        "Михайлов Олег Федорович"
            )

    val mockSettings = UserSettings(
        "Михайлов Олег Федорович",
        "c49a0b62-c192-11e1-8a03-f46d0490adee",
        "test",
        "http://172.16.17.242/torg_develop/hs/taskmgr/"
    )

//    val mockUser = User(
//        "c49a0b5b-c192-11e1-8a03-f46d0490adee",
//        "Администратор"
//    )

//    val mockSettings = UserSettings(
//        "Администратор",
//        "c49a0b5b-c192-11e1-8a03-f46d0490adee",
//        "123a",
//        "http://172.16.17.242/torg_develop/hs/taskmgr/"
//    )

    operator fun invoke(): Flow<UserSettings> = flow {
        emit(mockSettings)
    }
        .flowOn(ioDispatcher)

    fun getUserFlow(): Flow<User> = flow {
        emit(mockUser)
    }
        .catch {
            exceptionHandler(it)
        }.flowOn(ioDispatcher)

    suspend fun getUser(): User = flow {
        emit(mockUser)
    }.catch {
        exceptionHandler(it)
    }.flowOn(ioDispatcher)
        .first()

}