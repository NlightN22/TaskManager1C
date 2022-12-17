package space.active.taskmanager1c.presentation.utils

import android.content.Context
import android.view.MenuItem
import space.active.taskmanager1c.R

fun MenuItem.setIconAZState(state: Boolean, context: Context) {
    if (state) {
        this.icon = context.resources.getDrawable(R.drawable.ic_menu_order_za, context.resources.newTheme())
    } else {
        this.icon = context.resources.getDrawable(R.drawable.ic_menu_order_az, context.resources.newTheme())
    }
}

fun MenuItem.setIcon09State(state: Boolean, context: Context) {
    if (state) {
        this.icon = context.resources.getDrawable(R.drawable.ic_menu_order_90, context.resources.newTheme())
    } else {
        this.icon = context.resources.getDrawable(R.drawable.ic_menu_order_09, context.resources.newTheme())
    }
}