package space.active.taskmanager1c.domain.use_case

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.UserSettings
import javax.inject.Inject

class SaveUserSettingsToDataStore @Inject constructor(
    private val dataStore: DataStore<UserSettings>,
    private val exceptionHandler: ExceptionHandler,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    operator fun invoke(settings: UserSettings) =  CoroutineScope(ioDispatcher).launch {
        try {
            dataStore.updateData { settings }
        } catch (e: Throwable) {
            exceptionHandler(e)
        }
    }
}