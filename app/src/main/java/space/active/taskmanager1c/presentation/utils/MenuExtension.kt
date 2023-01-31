package space.active.taskmanager1c.presentation.utils

import android.content.Context
import android.view.MenuItem
import androidx.annotation.StringRes
import space.active.taskmanager1c.R

fun MenuItem.setIconAZState(desc: Boolean, context: Context) {
    if (desc) {
        this.icon = context.resources.getDrawable(R.drawable.ic_menu_order_za, context.resources.newTheme())
    } else {
        this.icon = context.resources.getDrawable(R.drawable.ic_menu_order_az, context.resources.newTheme())
    }
}

fun MenuItem.setIcon09State(desc: Boolean, context: Context) {
    if (desc) {
        this.icon = context.resources.getDrawable(R.drawable.ic_menu_order_90, context.resources.newTheme())
    } else {
        this.icon = context.resources.getDrawable(R.drawable.ic_menu_order_09, context.resources.newTheme())
    }
}

fun MenuItem.setTitleOrder(desc: Boolean, context: Context, @StringRes titleText: Int) {
    val ascendingText = context.getString(R.string.menu_order_asc)
    val descendingText = context.getString(R.string.menu_order_desc)
    this.title = if (desc) {
        context.getString(titleText, descendingText)
    } else {
        context.getString(titleText, ascendingText)
    }
}