package space.active.taskmanager1c.domain.use_case

import android.app.Application
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ExceptionHandler"

@Singleton
class ExceptionHandler @Inject constructor(
    private val showToast: ShowToast,
    private val context: Application,
    private val logger: Logger
) {
    private val _skipBackendException = MutableStateFlow<List<Throwable>>(emptyList())
    val skipBackendException = _skipBackendException.asStateFlow()
    private var backendCounter = 0
    private val maxBackendException = 1

    private val _sendExceptionEvent = MutableSharedFlow<BackendException>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val sendExceptionEvent = _sendExceptionEvent.asSharedFlow()

    operator fun invoke(e: Throwable) {
        when (e) {
            is NotCorrectServerAddress -> {
                showToast(e)
            }
            is AuthException -> {
                showToast(e)
                logger.error(TAG, "${e::class.java.simpleName} ${e.text.getString(context)}")
            }
            is EmptyObject -> {
                showToast(e)
                logger.error(TAG, "${e::class.java.simpleName} ${e.text.getString(context)}")
                e.printStackTrace()
            }
            is DbUnexpectedResult -> {
                showToast(e)
                e.printStackTrace()
            }
            is CantShowSnackBar -> {
                showToast(e)
                e.printStackTrace()
            }
            is ParseBackendException -> {
                showToast(e)
                e.message?.let { logger.log(TAG, "Message: $it") }
                e.cause?.let { logger.log(TAG, "Cause: $it") }
                e.printStackTrace()

            }
            is BackendException -> {
                logger.log(TAG, "Code: ${e.errorCode}")
                logger.log(TAG, "Body: ${e.errorBody}")
                e.sendToServerData?.let { logger.log(TAG, "BackendException: ${it}") }
                e.printStackTrace()
                backendCounter += 1
                if (_skipBackendException.value.contains(e)) {
                    logger.log(TAG, "skipped to show: $e")
                }else {
                    showToast(e)
                    if (backendCounter >= maxBackendException) {
                        logger.log(TAG, "Start dialog event")
                        _sendExceptionEvent.tryEmit(e)
                    }
                }
            }
            is ConnectionException -> {
                showToast(e)
                e.printStackTrace()
            }
            else -> {
                showToast(e)
                e.printStackTrace()
            }
        }
    }

    fun skipBackendExceptions(addException: Throwable) {
        _skipBackendException.value = _skipBackendException.value.plus(addException)
        logger.log(TAG, "Current exception list: ${_skipBackendException.value}")
    }
}