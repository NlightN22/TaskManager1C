package space.active.taskmanager1c.domain.use_case

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.UserSettings
import javax.inject.Inject

class SaveUserSettingsToDataStore @Inject constructor(
    private val dataStore: DataStore<UserSettings>,
    private val exceptionHandler: ExceptionHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke(settings: UserSettings) =  flow<Request<Any>> {
        emit(PendingRequest())
        try {
            dataStore.updateData { settings }
            emit(SuccessRequest(Any()))
        } catch (e: Throwable) {
            emit(ErrorRequest(e))
        }
    }.flowOn(ioDispatcher)
}