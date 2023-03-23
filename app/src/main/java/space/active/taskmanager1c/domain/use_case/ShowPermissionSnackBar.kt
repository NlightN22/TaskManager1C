package space.active.taskmanager1c.domain.use_case

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.CantShowSnackBar
import javax.inject.Inject

class ShowPermissionSnackBar @Inject constructor(
    private val exceptionHandler: ExceptionHandler
) {
    operator fun invoke(
        text: String,
        view: View,
        context: Context,
        listener: View.OnClickListener,
    ) {
        try {
            context.apply {
                val snack = Snackbar.make(view, text, 5000)
                snack.setActionTextColor(
                    resources.getColor(
                        R.color.button_not_pressed,
                        resources.newTheme()
                    )
                )
                snack.setAction(getString(R.string.snackbar_allow_button), listener)
                snack.show()
            }
        } catch (e: Throwable) {
            exceptionHandler(CantShowSnackBar())
        }
    }
}
