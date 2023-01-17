package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import space.active.taskmanager1c.coreutils.EmptyObject
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.Credentials
import space.active.taskmanager1c.domain.repository.SettingsRepository
import javax.inject.Inject

private const val TAG = "GetCredentials"

class GetCredentials @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val exceptionHandler: ExceptionHandler,
    private val logger: Logger
) {
    suspend operator fun invoke(): Credentials = coroutineScope {
        settingsRepository.getCredentials()
            .catch {
                if (it is EmptyObject) {
                    logger.log(TAG, "EmptyObject")
                    exceptionHandler(it)
                    coroutineContext.cancel()
                    delay(50)
                }
            }
            .first()
    }
}