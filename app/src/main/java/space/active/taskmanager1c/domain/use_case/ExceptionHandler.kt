package space.active.taskmanager1c.domain.use_case

import android.app.Application
import android.content.Context
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import javax.inject.Inject

private const val TAG = "ExceptionHandler"

class ExceptionHandler @Inject constructor(
    private val showErrorToast: ShowErrorToast,
    private val context: Application,
    private val logger: Logger
) {
    operator fun invoke(e: Throwable) {
        when (e) {
            is NotCorrectServerAddress -> {
                showErrorToast(e)
            }
            is AuthException -> {
                showErrorToast(e)
                logger.error(TAG, "${e::class.java.simpleName} ${e.text.getString(context)}")
            }
            is EmptyObject -> {
                showErrorToast(e)
                logger.error(TAG, "${e::class.java.simpleName} ${e.text.getString(context)}")
                e.printStackTrace()
            }
            is DbUnexpectedResult -> {
                showErrorToast(e)
                e.printStackTrace()
            }
            is CantShowSnackBar -> {
                showErrorToast(e)
                e.printStackTrace()
            }
            is ParseBackendException -> {
                showErrorToast(e)
                e.printStackTrace()

            }
            is BackendException -> {
                showErrorToast(e)
                logger.log(TAG, "Code: ${e.errorCode}")
                logger.log(TAG, "Body: ${e.errorBody}")
                e.printStackTrace()
            }
            is ConnectionException -> {
                showErrorToast(e)
                e.printStackTrace()
            }
            else -> {
                showErrorToast(e)
                e.printStackTrace()
            }
        }
    }
}