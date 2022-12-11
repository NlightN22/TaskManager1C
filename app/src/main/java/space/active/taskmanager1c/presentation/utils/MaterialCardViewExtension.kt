package space.active.taskmanager1c.presentation.utils

import android.content.res.ColorStateList
import com.google.android.material.card.MaterialCardView
import space.active.taskmanager1c.R

fun MaterialCardView.setState(enabled: Boolean) {
    val enabledBackgroundColor: ColorStateList = resources.getColorStateList(R.color.editable_text_background, resources.newTheme())
    val disabledBackgroundColor: ColorStateList = resources.getColorStateList(R.color.not_editable_text_background, resources.newTheme())
    val strokeColor: Int = resources.getColor(R.color.button_not_pressed, resources.newTheme())
    val strokeDisabledColor: Int = resources.getColor(R.color.not_editable_text_background, resources.newTheme())
    if (enabled) {
        this.backgroundTintList = enabledBackgroundColor
        this.strokeWidth = resources.getDimension(R.dimen.editable_card_stroke_width).toInt()
        this.strokeColor = strokeColor
    } else {
        this.backgroundTintList = disabledBackgroundColor
        this.strokeColor = strokeDisabledColor
    }
}