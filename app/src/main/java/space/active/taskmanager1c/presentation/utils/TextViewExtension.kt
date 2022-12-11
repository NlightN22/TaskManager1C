package space.active.taskmanager1c.presentation.utils

import android.widget.TextView
import space.active.taskmanager1c.R

fun TextView.setColorState(state: Boolean) {
    val colorDisabled: Int = resources.getColor(R.color.title_not_editable, resources.newTheme())
    val colorEnabled: Int = resources.getColor(R.color.title_editable, resources.newTheme())
    if (state) {
        this.setTextColor(colorEnabled)
    }
    else {
        this.setTextColor(colorDisabled)
    }
}