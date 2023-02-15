package space.active.taskmanager1c.presentation.utils

import android.content.Context
import android.widget.Toast
import space.active.taskmanager1c.coreutils.UiText

class ToastsImpl(
    private val appContext: Context
): Toasts {
    override fun invoke(message: String) {
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
    }

    override fun invoke(uiText: UiText) {
        Toast.makeText(appContext,uiText.getString(appContext),Toast.LENGTH_SHORT).show()
    }
}