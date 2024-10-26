package space.active.taskmanager1c.presentation.utils

import android.content.Context
import android.widget.Toast
import space.active.taskmanager1c.coreutils.UiText
import android.view.Gravity

class ToastsImpl(
    private val appContext: Context,
    private val yOffset: Int = -500
): Toasts {
    override fun invoke(message: String) {
        val toast = Toast.makeText(appContext, message, Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, yOffset)
        toast.show()
    }

    override fun invoke(uiText: UiText) {
        val toast = Toast.makeText(appContext, uiText.getString(appContext), Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, yOffset)
        toast.show()
    }
    }