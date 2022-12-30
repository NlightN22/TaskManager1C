package space.active.taskmanager1c.domain.use_case

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import space.active.taskmanager1c.coreutils.EmptyObject
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.User
import space.active.taskmanager1c.domain.models.UserSettings
import javax.inject.Inject

class GetUserSettingsFromDataStore @Inject constructor(
    private val exceptionHandler: ExceptionHandler,
    private val dataStore: DataStore<UserSettings>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke(): Flow<UserSettings> = dataStore.data.catch {
            exceptionHandler(it)
        }.flowOn(ioDispatcher)

    fun getUserFlow(): Flow<User> = dataStore.data.map {
        if (it.username == null || it.userId == null) {
            throw EmptyObject("UserFromSettings")
        } else {
            User(
                id = it.userId!!, name = it.username!!
            )
        }
    }.catch {
        exceptionHandler(it)
    }.flowOn(ioDispatcher)

    suspend fun getUser(): User = dataStore.data.map {
        if (it.username == null || it.userId == null) {
            throw EmptyObject("UserFromSettings")
        } else {
            User(
                id = it.userId!!, name = it.username!!
            )
        }
    }.catch {
        exceptionHandler(it)
    }.flowOn(ioDispatcher)
        .first()

}