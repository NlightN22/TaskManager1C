package space.active.taskmanager1c.presentation.utils

import android.content.Context
import android.widget.Toast

class ToastsImpl(
    private val appContext: Context
): Toasts {
    override fun toast(message: String) {
        Toast.makeText(appContext, message, Toast.LENGTH_SHORT).show()
    }
}