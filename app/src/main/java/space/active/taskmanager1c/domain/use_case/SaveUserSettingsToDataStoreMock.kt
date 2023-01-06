package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.data.local.db.tasks_room_db.local_entities.UserSettings
import javax.inject.Inject

class SaveUserSettingsToDataStore @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke(settings: UserSettings) = flow<Request<Any>> {
        emit(PendingRequest())
        delay(100)
        emit(SuccessRequest(Any()))

    }.flowOn(ioDispatcher)
}
