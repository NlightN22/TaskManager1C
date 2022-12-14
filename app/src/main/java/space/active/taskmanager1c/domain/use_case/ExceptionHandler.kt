package space.active.taskmanager1c.domain.use_case

import space.active.taskmanager1c.coreutils.*
import javax.inject.Inject

class ExceptionHandler @Inject constructor(
    private val showErrorToast: ShowErrorToast
) {
    operator fun invoke(e: AppExceptions) {
        when (e) {
            is AuthException -> {}
            is EmptyObject -> {
                showErrorToast(e, startText = "Unexpected null or empty object")
            }
            is ServerNoAnswer -> {}
            is NullAnswerFromServer -> {}
            is DbUnexpectedResult -> {
                showErrorToast(e)
            }
            is ThisTaskIsNotEdited -> {}
            is ThisTaskIsNotNew -> {}
            is TaskIsNewAndInSendingState -> {}
            is TaskHasNotCorrectState -> {}
            is JobIsNotStarted -> {}
        }
    }
}