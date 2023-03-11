package space.active.taskmanager1c.presentation.utils.dialogs

import android.app.AlertDialog
import android.content.Context
import dagger.hilt.android.scopes.FragmentScoped
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.TaskDomain
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import javax.inject.Inject

private const val TAG = "TaskStatusDialog"

@FragmentScoped
class TaskStatusDialog @Inject constructor(
    private val exceptionHandler: ExceptionHandler,
    private val logger: Logger
) {
    private var currentDialog: AlertDialog? = null
    fun showDialog(
        params: DialogParams,
        context: Context,
        positive: () -> Unit,
    ) {
        if (currentDialog != null) {
            if (currentDialog!!.isShowing) return
        }
        logger.log(TAG, "showSendErrorDialog")
        try {
            val statusString: String = context.getString(params.newStatus.getResId())
            val message: String =
                context.getString(R.string.status_dialog_message,params.taskName)
            val title: String = context.getString(R.string.status_dialog_title,statusString)
            currentDialog = AlertDialog.Builder(context)
                .setMessage(message)
                .setTitle(title)
                .setCancelable(true)
                .setPositiveButton(R.string.status_dialog_ok) { _, _ ->
                    positive()
                }
                .setNegativeButton(R.string.status_dialog_cancel) { _, _ ->
                }
                .create()
            currentDialog!!.show()
        } catch (e: Exception) {
            exceptionHandler(e)
        }

    }

    data class DialogParams(
        val taskName: String,
        val newStatus: TaskDomain.Status,
    )
}