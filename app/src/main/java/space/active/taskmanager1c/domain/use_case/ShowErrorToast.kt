package space.active.taskmanager1c.domain.use_case

import android.app.Application
import android.os.Looper
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.presentation.utils.Toasts
import javax.inject.Inject

private const val TAG = "ShowErrorToast"

class ShowErrorToast @Inject constructor(
    private val toast: Toasts,
) {
    operator fun invoke(e: Throwable) {
        android.os.Handler(Looper.getMainLooper()).post {
            val message = "ERROR:  ${e.message} "
            toast.toast(message)
        }
    }
}