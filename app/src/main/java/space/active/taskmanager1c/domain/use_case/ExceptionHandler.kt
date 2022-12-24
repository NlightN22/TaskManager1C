package space.active.taskmanager1c.domain.use_case

import space.active.taskmanager1c.coreutils.*
import javax.inject.Inject

private const val TAG = "ExceptionHandler"
class ExceptionHandler @Inject constructor(
    private val showErrorToast: ShowErrorToast,
    private val logger: space.active.taskmanager1c.coreutils.logger.Logger
) {
    operator fun invoke(e: Throwable) {
        when (e) {
            is AuthException -> {}
            is EmptyObject -> {
                showErrorToast(e)
                logger.error(TAG, "${e::class.java.simpleName} ${e.message}")
            }
            is ServerNoAnswer -> {}
            is NullAnswerFromServer -> {}
            is DbUnexpectedResult -> {
                showErrorToast(e)
                logger.error(TAG, "${e::class.java.simpleName} ${e.message}")
            }
            is ThisTaskIsNotEdited -> {}
            is ThisTaskIsNotNew -> {}
            is TaskIsNewAndInSendingState -> {}
            is TaskHasNotCorrectState -> {}
            is JobIsNotStarted -> {}
            is ParseBackendException -> {
                showErrorToast(e)
                logger.error(TAG, "${e::class.java.simpleName} ${e.inEx.message}")

            }
            is BackendException -> {
                showErrorToast(e)
                logger.error(TAG, "${e::class.java.simpleName} \nCODE: ${e.errorCode} \nBODY: ${e.errorBody}")
            }
            is ConnectionException -> {
                showErrorToast(e)
                logger.error(TAG, "${e::class.java.simpleName} ${e.inEx.message}")
            }
            else -> {showErrorToast(e)}
        }
    }
}