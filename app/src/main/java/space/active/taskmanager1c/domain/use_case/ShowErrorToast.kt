package space.active.taskmanager1c.domain.use_case

import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.presentation.utils.Toasts
import javax.inject.Inject

private const val TAG = "ShowErrorToast"

class ShowErrorToast @Inject constructor(
    private val toast: Toasts,
    private val logger: Logger
) {

    operator fun invoke(e: Throwable, startText: String = "", endText: String = "") {
        val message = "ERROR: $startText ${e.message} $endText"
        toast.toast(message)
        logger.error(TAG, message)
    }
}