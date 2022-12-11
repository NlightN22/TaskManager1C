package space.active.taskmanager1c.presentation.utils

import android.content.res.ColorStateList
import android.widget.MultiAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import space.active.taskmanager1c.R

fun TextInputLayout.setState(enabled: Boolean, editable: Boolean = false) {
    val hintColorDisabled: ColorStateList =
        resources.getColorStateList(R.color.title_not_editable, resources.newTheme())
    val hintColorEnabled: ColorStateList =
        resources.getColorStateList(R.color.title_editable, resources.newTheme())
    if (enabled) {
        this.defaultHintTextColor = hintColorEnabled
    } else {
        this.defaultHintTextColor =
            hintColorDisabled // I have been looking for this for a very long time!!!
    }
    this.editText?.let {
        if (it is TextInputEditText) {
            it.setState(enabled, editable = editable)
        } else if (it is MultiAutoCompleteTextView) {
            it.setState(enabled, editable = editable)
        }
    }
}

private fun TextInputEditText.setState(enabled: Boolean, editable: Boolean = false) {
    val enabledBackgroundColor: ColorStateList =
        resources.getColorStateList(R.color.editable_text_background, resources.newTheme())
    val disabledBackgroundColor: ColorStateList =
        resources.getColorStateList(R.color.not_editable_text_background, resources.newTheme())
    val editableTextColor: Int = resources.getColor(R.color.editable_text, resources.newTheme())
    val notEditableTextColor: Int =
        resources.getColor(R.color.not_editable_text, resources.newTheme())
    this.isEnabled = true
    this.isClickable = true
    this.isFocusable = editable
    if (enabled) {
        this.backgroundTintList = enabledBackgroundColor
        this.setTextColor(editableTextColor)
    } else {
        this.backgroundTintList = disabledBackgroundColor
        this.setTextColor(notEditableTextColor)
    }
}

private fun MultiAutoCompleteTextView.setState(enabled: Boolean, editable: Boolean = false) {
    val enabledBackgroundColor: ColorStateList =
        resources.getColorStateList(R.color.editable_text_background, resources.newTheme())
    val disabledBackgroundColor: ColorStateList =
        resources.getColorStateList(R.color.not_editable_text_background, resources.newTheme())
    val editableTextColor: Int = resources.getColor(R.color.editable_text, resources.newTheme())
    val notEditableTextColor: Int =
        resources.getColor(R.color.not_editable_text, resources.newTheme())
    this.isEnabled = true
    this.isClickable = true
    if (enabled) {
        this.backgroundTintList = enabledBackgroundColor
        this.setTextColor(editableTextColor)
        this.isFocusable = editable
    } else {
        this.backgroundTintList = disabledBackgroundColor
        this.setTextColor(notEditableTextColor)
        this.isFocusable = false
    }
}