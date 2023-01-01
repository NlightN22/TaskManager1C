package space.active.taskmanager1c.domain.use_case

import android.app.Application
import android.os.Looper
import space.active.taskmanager1c.coreutils.AppExceptions
import space.active.taskmanager1c.presentation.utils.Toasts
import javax.inject.Inject

private const val TAG = "ShowErrorToast"

class ShowErrorToast @Inject constructor(
    private val toast: Toasts,
    private val context: Application
) {
    operator fun invoke(e: AppExceptions) {
        try {
            android.os.Handler(Looper.getMainLooper()).post {
                val message = e.text.getString(context)
                toast(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    operator fun invoke(e: Throwable) {
        try {
            android.os.Handler(Looper.getMainLooper()).post {
                toast(e.message.toString())
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}